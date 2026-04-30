package org.game24.marketsync.game.hook;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.model.DeliveryResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerPointsHook {

    private final JavaPlugin plugin;

    private volatile PlayerPointsAPI api;

    public PlayerPointsHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<DeliveryResult> givePoints(String username, int amount) {
        PlayerPointsAPI api = api();
        if (api == null) {
            plugin.getSLF4JLogger().error("PlayerPoints is not initialized");
            return CompletableFuture.completedFuture(DeliveryResult.FAILED);
        }

        UUID uuid = api.getAccountUUIDByName(username);
        if (uuid == null) {
            plugin.getSLF4JLogger().warn("Cannot find player {} to give points", username);
            return CompletableFuture.completedFuture(DeliveryResult.FAILED);
        }

        if (api.give(uuid, amount)) {
            plugin.getSLF4JLogger().info("Player {} received {} points", username, amount);
            return CompletableFuture.completedFuture(DeliveryResult.COMPLETED);
        }

        plugin.getSLF4JLogger().error("Fail to give points to player {}", username);
        return CompletableFuture.completedFuture(DeliveryResult.INCOMPLETED);
    }

    private PlayerPointsAPI api() {
        if (api == null) {
            synchronized (this) {
                if (api == null) {
                    api = PlayerPoints.getInstance().getAPI();
                }
            }
        }

        return api;
    }
}
