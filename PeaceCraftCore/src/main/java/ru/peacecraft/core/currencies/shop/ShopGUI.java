package ru.peacecraft.core.currencies.shop;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.peacecraft.core.currencies.model.ShopItem;

public final class ShopGUI implements InventoryHolder {

    private static final String GUI_TITLE = "§6§lМагазин";
    private static final int GUI_SIZE = 54;

    private final List<ShopItem> items;
    private Inventory inventory;

    private ShopGUI(List<ShopItem> items) {
        this.items = List.copyOf(items);
    }

    public static ShopGUI create(List<ShopItem> items) {
        ShopGUI gui = new ShopGUI(items);
        gui.inventory = Bukkit.createInventory(gui, GUI_SIZE, GUI_TITLE);
        gui.populateInventory();
        return gui;
    }

    private void populateInventory() {
        int slot = 0;

        for (ShopItem item : items) {
            if (slot >= GUI_SIZE - 9) {
                break;
            }
            inventory.setItem(slot, item.createItemStack());
            slot++;
        }

        for (int i = slot; i < GUI_SIZE - 9; i++) {
            inventory.setItem(i, createFillerItem());
        }

        setupNavigationRow();
    }

    private ItemStack createFillerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private void setupNavigationRow() {}

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public List<ShopItem> getItems() {
        return items;
    }
}
