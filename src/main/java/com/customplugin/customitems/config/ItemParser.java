package com.customplugin.customitems.config;

import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.Activator;
import com.customplugin.customitems.model.CustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a single item YAML file into a {@link CustomItem}.
 * All parsing errors result in a null return (item is skipped).
 */
public final class ItemParser {

    /** PDC key used to stamp every custom item stack. */
    public static final String PDC_NAMESPACE = "customitems";
    public static final String PDC_KEY       = "id";

    private static final Pattern DURATION_PATTERN =
            Pattern.compile("^(\\d+)(s|m|h|t)?$", Pattern.CASE_INSENSITIVE);

    private final NamespacedKey pdcKey;
    private final Logger logger;

    public ItemParser(NamespacedKey pdcKey, Logger logger) {
        this.pdcKey = pdcKey;
        this.logger = logger;
    }

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    /**
     * Parse a YAML file into a CustomItem. Returns {@code null} on any error.
     */
    public CustomItem parse(File file) {
        YamlConfiguration cfg;
        try {
            cfg = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            logger.severe("[CustomItems] Failed to load YAML file: " + file.getName() + " — " + e.getMessage());
            return null;
        }

        String id = cfg.getString("id");
        if (id == null || id.isBlank()) {
            logger.severe("[CustomItems] Missing 'id' field in: " + file.getName());
            return null;
        }

        // Build base ItemStack
        ItemStack item = buildItemStack(cfg, id, file.getName());
        if (item == null) return null;

        // Parse activators
        Map<String, Activator> activators = parseActivators(cfg, id, file.getName());

        return new CustomItem(id, item, activators);
    }

    // -------------------------------------------------------------------------
    // ItemStack construction
    // -------------------------------------------------------------------------

    private ItemStack buildItemStack(YamlConfiguration cfg, String id, String fileName) {
        ConfigurationSection sec = cfg.getConfigurationSection("item");
        if (sec == null) {
            logger.severe("[CustomItems] Missing 'item' section in: " + fileName);
            return null;
        }

        String materialName = sec.getString("material", "STONE").toUpperCase();
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            logger.severe("[CustomItems] Unknown material '" + materialName + "' in: " + fileName);
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            logger.severe("[CustomItems] Material '" + materialName + "' cannot have meta in: " + fileName);
            return null;
        }

        // Name (MiniMessage + legacy color codes)
        String rawName = sec.getString("name");
        if (rawName != null) {
            meta.displayName(parseText(rawName));
        }

        // Lore
        List<String> rawLore = sec.getStringList("lore");
        if (!rawLore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : rawLore) {
                loreComponents.add(parseText(line));
            }
            meta.lore(loreComponents);
        }

        // Custom model data
        if (sec.contains("custom_model_data")) {
            meta.setCustomModelData(sec.getInt("custom_model_data"));
        }

        // Unbreakable
        if (sec.getBoolean("unbreakable", false)) {
            meta.setUnbreakable(true);
        }

        // Item flags
        for (String flagName : sec.getStringList("flags")) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flagName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warning("[CustomItems] Unknown item flag '" + flagName + "' in: " + fileName);
            }
        }

        // Stamp PDC id
        meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }

    // -------------------------------------------------------------------------
    // Activator parsing
    // -------------------------------------------------------------------------

    private Map<String, Activator> parseActivators(YamlConfiguration cfg, String id, String fileName) {
        Map<String, Activator> result = new HashMap<>();
        ConfigurationSection activatorsSec = cfg.getConfigurationSection("activators");
        if (activatorsSec == null) return result;

        for (String activatorType : activatorsSec.getKeys(false)) {
            ConfigurationSection aSec = activatorsSec.getConfigurationSection(activatorType);
            if (aSec == null) continue;

            // Cooldown
            String cooldownRaw = aSec.getString("cooldown", "0s");
            long cooldownTicks = parseDuration(cooldownRaw);

            String cooldownMessage = aSec.getString("cooldown_message",
                    "&cYou must wait before using this again!");

            // Conditions
            List<String> conditions = aSec.getStringList("conditions");

            // Actions
            List<ActionDefinition> actions = parseActions(aSec.getList("actions"), fileName);

            Activator activator = new Activator(
                    activatorType.toUpperCase(),
                    cooldownTicks,
                    cooldownMessage,
                    conditions,
                    actions
            );
            result.put(activatorType.toUpperCase(), activator);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Action parsing
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<ActionDefinition> parseActions(List<?> rawList, String fileName) {
        List<ActionDefinition> result = new ArrayList<>();
        if (rawList == null) return result;

        for (Object entry : rawList) {
            if (!(entry instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) entry;

            Object typeObj = map.get("type");
            if (typeObj == null) {
                logger.warning("[CustomItems] Action missing 'type' field in: " + fileName);
                continue;
            }

            String type = typeObj.toString().toUpperCase();
            Map<String, Object> params = new HashMap<>(map);
            params.remove("type");

            // Extract nested action lists before building params map
            List<ActionDefinition> thenActions = null;
            List<ActionDefinition> elseActions = null;

            Object thenRaw = params.remove("then");
            Object elseRaw = params.remove("else");

            if (thenRaw instanceof List) {
                thenActions = parseActions((List<?>) thenRaw, fileName);
            }
            if (elseRaw instanceof List) {
                elseActions = parseActions((List<?>) elseRaw, fileName);
            }

            result.add(new ActionDefinition(type, params, thenActions, elseActions));
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Parses duration strings: 5s → 100 ticks, 2m → 2400, 1h → 72000, 10t → 10.
     */
    public static long parseDuration(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        Matcher m = DURATION_PATTERN.matcher(raw.trim());
        if (!m.matches()) return 0;
        long value = Long.parseLong(m.group(1));
        String unit = m.group(2);
        if (unit == null || unit.equalsIgnoreCase("t")) return value;
        return switch (unit.toLowerCase()) {
            case "s" -> value * 20L;
            case "m" -> value * 1200L;
            case "h" -> value * 72000L;
            default -> value;
        };
    }

    /**
     * Parses text with MiniMessage tags and legacy {@code &} color codes.
     */
    public static Component parseText(String raw) {
        if (raw == null) return Component.empty();
        // Convert legacy & codes first
        String converted = raw.replace("&", "§");
        // Then parse MiniMessage (handles hex <#RRGGBB> tags)
        try {
            return MiniMessage.miniMessage().deserialize(raw);
        } catch (Exception e) {
            // Fallback to legacy
            return LegacyComponentSerializer.legacySection().deserialize(converted);
        }
    }
}
