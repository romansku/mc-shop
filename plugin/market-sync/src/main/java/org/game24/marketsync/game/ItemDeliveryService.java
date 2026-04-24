package org.game24.marketsync.game;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;
import org.game24.marketsync.model.Item;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ItemDeliveryService {

    private final JavaPlugin plugin;

    public ItemDeliveryService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public @NonNull CompletableFuture<DeliveryResult> completeItemDelivery(@NonNull Delivery delivery, @NonNull Item item) {
        CompletableFuture<DeliveryResult> resultFuture = new CompletableFuture<>();

        Logger logger = plugin.getSLF4JLogger();
        long orderId = delivery.getOrderId();
        long itemId = delivery.getItemId();

        String username = delivery.getUsername();
        Player onlinePlayer = Bukkit.getPlayerExact(username);
        if (onlinePlayer == null) {
            logger.debug("Player {} for order {} is offline", username, orderId);
            resultFuture.complete(DeliveryResult.INCOMPLETED);
            return resultFuture;
        }

        String itemMaterial = item.getData();
        NamespacedKey key = NamespacedKey.fromString(itemMaterial);
        if (key == null) {
            logger.error("Cannot build item from string {}", itemMaterial);
            resultFuture.complete(DeliveryResult.INCOMPLETED);
            return resultFuture;
        }

        Material material = Registry.MATERIAL.get(key);
        if (material == null || !material.isItem()) {
            logger.error("Material {} is not a valid item", itemMaterial);
            resultFuture.complete(DeliveryResult.INCOMPLETED);
            return resultFuture;
        }

        ItemStack itemStack = new ItemStack(material, item.getCount());

        onlinePlayer.getScheduler().run(plugin, task -> {
            if (task.isCancelled()) {
                return;
            }
            PlayerInventory inventory = onlinePlayer.getInventory();
            if (hasSpace(inventory, itemStack)) {
                inventory.addItem(itemStack);
                resultFuture.complete(DeliveryResult.COMPLETED);
            } else {
                resultFuture.complete(DeliveryResult.INCOMPLETED);
            }

        }, () -> logOffline(username, orderId, itemId, resultFuture));

        return resultFuture
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(e -> {
                    plugin.getSLF4JLogger().error(
                            "ItemDelivery error; order: {}, item: {}, user: {}", orderId, itemId, username, e);
                    return DeliveryResult.FAILED;
                });
    }

    private void logOffline(String username, long orderId, long itemId, CompletableFuture<DeliveryResult> resultFuture) {
        plugin.getSLF4JLogger()
                .debug("Player {} have leaved; order {}, item {} is not delivered", username, orderId, itemId);
        resultFuture.complete(DeliveryResult.INCOMPLETED);
    }

    // Метод для проверки места
    private boolean hasSpace(Inventory inv, ItemStack item) {
        int freeSpace = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack slot = inv.getItem(i);

            if (slot == null || slot.getType().isAir()) {
                freeSpace += item.getMaxStackSize();
            } else if (slot.isSimilar(item)) {
                freeSpace += item.getMaxStackSize() - slot.getAmount();
            }
            if (freeSpace >= item.getAmount()) {
                return true;
            }
        }
        return false;
    }
}
