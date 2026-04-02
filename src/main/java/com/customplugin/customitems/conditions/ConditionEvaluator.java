package com.customplugin.customitems.conditions;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.util.TextUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates string-based condition expressions.
 *
 * Supported operators: &&  ||  !
 * Supported comparisons: > < >= <= == !=
 *
 * Examples:
 *   "%player_health% > 10"
 *   "%player_health% < 10 && %world% == 'world'"
 *   "!%player_is_sneaking%"
 */
public final class ConditionEvaluator {

    private static final Pattern COMPARISON_PATTERN =
            Pattern.compile("(.+?)\\s*(>=|<=|==|!=|>|<)\\s*(.+)");

    private final CustomItemsPlugin plugin;

    public ConditionEvaluator(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public boolean evaluate(String expression, ExecutionContext context) {
        if (expression == null || expression.isBlank()) return true;

        // Resolve placeholders first
        String resolved = TextUtil.resolvePlaceholders(expression.trim(), context, plugin);
        plugin.debugLog("Evaluating condition: '" + expression + "' → '" + resolved + "'");

        return parseExpression(resolved);
    }

    // -------------------------------------------------------------------------
    // Recursive descent parser
    // -------------------------------------------------------------------------

    /** Handles || (lowest precedence) */
    private boolean parseExpression(String expr) {
        expr = expr.trim();
        // Split on || only at top level (not inside quotes or parens)
        int[] split = findTopLevelOperator(expr, "||");
        if (split != null) {
            return parseExpression(expr.substring(0, split[0]))
                    || parseExpression(expr.substring(split[1]));
        }
        return parseTerm(expr);
    }

    /** Handles && */
    private boolean parseTerm(String expr) {
        expr = expr.trim();
        int[] split = findTopLevelOperator(expr, "&&");
        if (split != null) {
            return parseTerm(expr.substring(0, split[0]))
                    && parseTerm(expr.substring(split[1]));
        }
        return parseFactor(expr);
    }

    /** Handles ! and parentheses */
    private boolean parseFactor(String expr) {
        expr = expr.trim();

        // Negation
        if (expr.startsWith("!")) {
            return !parseFactor(expr.substring(1));
        }

        // Parentheses
        if (expr.startsWith("(") && expr.endsWith(")")) {
            return parseExpression(expr.substring(1, expr.length() - 1));
        }

        return evaluateAtom(expr);
    }

    /** Evaluates a single comparison or boolean literal */
    private boolean evaluateAtom(String expr) {
        expr = expr.trim();

        // Boolean literals
        if (expr.equalsIgnoreCase("true"))  return true;
        if (expr.equalsIgnoreCase("false")) return false;

        // Comparison
        Matcher m = COMPARISON_PATTERN.matcher(expr);
        if (m.matches()) {
            String left  = m.group(1).trim();
            String op    = m.group(2).trim();
            String right = m.group(3).trim();

            // Strip surrounding single/double quotes from string literals
            left  = stripQuotes(left);
            right = stripQuotes(right);

            return compare(left, op, right);
        }

        // Truthy string check (non-empty, non-false, non-zero)
        return isTruthy(expr);
    }

    // -------------------------------------------------------------------------
    // Comparison logic
    // -------------------------------------------------------------------------

    private boolean compare(String left, String op, String right) {
        // Try numeric comparison first
        try {
            double l = Double.parseDouble(left);
            double r = Double.parseDouble(right);
            return switch (op) {
                case ">"  -> l > r;
                case "<"  -> l < r;
                case ">=" -> l >= r;
                case "<=" -> l <= r;
                case "==" -> l == r;
                case "!=" -> l != r;
                default   -> false;
            };
        } catch (NumberFormatException ignored) {
            // Fall through to string comparison
        }

        // String comparison
        int cmp = left.compareToIgnoreCase(right);
        return switch (op) {
            case "==" -> left.equalsIgnoreCase(right);
            case "!=" -> !left.equalsIgnoreCase(right);
            case ">"  -> cmp > 0;
            case "<"  -> cmp < 0;
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            default   -> false;
        };
    }

    private boolean isTruthy(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("false")) return false;
        try { return Double.parseDouble(value) != 0; } catch (NumberFormatException ignored) {}
        return true;
    }

    private String stripQuotes(String s) {
        if (s.length() >= 2
                && ((s.startsWith("'") && s.endsWith("'"))
                 || (s.startsWith("\"") && s.endsWith("\"")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    // -------------------------------------------------------------------------
    // Top-level operator search (respects parentheses and quotes)
    // -------------------------------------------------------------------------

    /**
     * Finds the position of the LAST top-level occurrence of {@code op} (left-associative).
     * Returns null if not found.
     */
    private int[] findTopLevelOperator(String expr, String op) {
        int depth = 0;
        boolean inSingle = false;
        boolean inDouble = false;

        int[] lastFound = null;

        for (int i = 0; i <= expr.length() - op.length(); i++) {
            char c = expr.charAt(i);

            if (c == '\'' && !inDouble) inSingle = !inSingle;
            if (c == '"'  && !inSingle) inDouble = !inDouble;
            if (inSingle || inDouble) continue;

            if (c == '(') depth++;
            if (c == ')') depth--;

            if (depth == 0 && expr.startsWith(op, i)) {
                lastFound = new int[]{ i, i + op.length() };
                i += op.length() - 1;
            }
        }
        return lastFound;
    }
}
