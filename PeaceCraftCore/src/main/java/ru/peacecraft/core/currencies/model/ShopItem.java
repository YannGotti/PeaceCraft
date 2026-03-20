package ru.peacecraft.core.currencies.model;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ShopItem {

    private final String id;
    private final String displayName;
    private final Material material;
    private final int customModelData;
    private final double buyPrice;
    private final double sellPrice;
    private final String currencyId;
    private final List<String> lore;

    public ShopItem(
        String id,
        String displayName,
        Material material,
        int customModelData,
        double buyPrice,
        double sellPrice,
        String currencyId,
        List<String> lore
    ) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.customModelData = customModelData;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.currencyId = currencyId;
        this.lore = lore;
    }

    public static ShopItem fromConfig(String id, ConfigurationSection config) {
        return new ShopItem(
            id,
            config.getString("display-name", "Unknown Item"),
            Material.valueOf(config.getString("material", "STONE")),
            config.getInt("custom-model-data", 0),
            config.getDouble("buy-price", 0),
            config.getDouble("sell-price", 0),
            config.getString("currency", "scrap"),
            config.getStringList("lore")
        );
    }

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(displayName);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public List<String> getLore() {
        return lore;
    }
}
