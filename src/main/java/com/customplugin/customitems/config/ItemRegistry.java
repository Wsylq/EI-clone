package com.customplugin.customitems.config;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.model.CustomItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry for all loaded {@link CustomItem} definitions.
 * Loads from /plugins/CustomItems/items/*.yml on startup and reload.
 */
public final class ItemRegistry {

    private final CustomItemsPlugin plugin;
    private final NamespacedKey pdcKey;
    private final ItemParser parser;

    /** Thread-safe read map — replaced atomically on reload. */
    private volatile Map<String, CustomItem> items = Collections.emptyMap();

    public ItemRegistry(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.pdcKey = new NamespacedKey(plugin, ItemParser.PDC_KEY);
        this.parser = new ItemParser(pdcKey, plugin.getLogger());
    }

    // -------------------------------------------------------------------------
    // Loading
    // -------------------------------------------------------------------------

    /**
     * (Re)loads all item YAMLs from the items/ directory.
     * Old definitions are fully replaced.
     */
    public void loadAll() {
        File itemsDir = new File(plugin.getDataFolder(), "items");
        if (!itemsDir.exists() || !itemsDir.isDirectory()) {
            plugin.getLogger().warning("[CustomItems] items/ directory not found — no items loaded.");
            items = Collections.emptyMap();
            return;
        }

        File[] files = itemsDir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("[CustomItems] No item files found in items/.");
            items = Collections.emptyMap();
            return;
        }

        Map<String, CustomItem> loaded = new HashMap<>();
        int errors = 0;

        for (File file : files) {
            try {
                CustomItem item = parser.parse(file);
                if (item == null) {
                    errors++;
                    continue;
                }
                if (loaded.containsKey(item.getId())) {
                    plugin.getLogger().warning("[CustomItems] Duplicate item id '" + item.getId()
                            + "' in file " + file.getName() + " — skipping.");
                    errors++;
                    continue;
                }
                loaded.put(item.getId(), item);
                plugin.debugLog("Loaded item: " + item.getId());
            } catch (Exception e) {
                plugin.getLogger().severe("[CustomItems] Unexpected error loading "
                        + file.getName() + ": " + e.getMessage());
                errors++;
            }
        }

        // Atomic replacement
        items = Collections.unmodifiableMap(loaded);

        plugin.getLogger().info("[CustomItems] Loaded " + loaded.size()
                + " item(s), " + errors + " error(s).");
    }

    // -------------------------------------------------------------------------
    // Lookups
    // -------------------------------------------------------------------------

    public Optional<CustomItem> getById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(items.get(id));
    }

    /**
     * Reads the PDC stamp from an ItemStack and resolves to a CustomItem.
     * Returns empty if the item is null, has no PDC, or the id is unregistered.
     */
    public Optional<CustomItem> fromItemStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return Optional.empty();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return Optional.empty();

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(pdcKey, PersistentDataType.STRING)) return Optional.empty();

        String id = pdc.get(pdcKey, PersistentDataType.STRING);
        return getById(id);
    }

    public Collection<CustomItem> getAll() {
        return items.values();
    }

    public int getLoadedCount() {
        return items.size();
    }

    public NamespacedKey getPdcKey() {
        return pdcKey;
    }

    public ItemParser getParser() {
        return parser;
    }
}
