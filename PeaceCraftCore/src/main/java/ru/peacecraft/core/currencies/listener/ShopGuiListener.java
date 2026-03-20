package ru.peacecraft.core.currencies.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import ru.peacecraft.core.currencies.shop.ShopGUI;

public final class ShopGuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();

        if (!(topInventory.getHolder() instanceof ShopGUI)) {
            return;
        }

        // Полностью запрещаем любые shift-click, number key swap, collect to cursor и т.п.
        event.setCancelled(true);

        // На будущее: здесь потом можно будет обработать клики по товарам
        // if (event.getClickedInventory() == topInventory) { ... }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();

        if (!(topInventory.getHolder() instanceof ShopGUI)) {
            return;
        }

        int topSize = topInventory.getSize();

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
