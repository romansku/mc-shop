package org.game24.marketsync.game;

import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;
import org.game24.marketsync.model.Item;

import java.util.concurrent.CompletableFuture;

public class CommandDeliveryService {

    private static final String BUKKIT_CMD = "BUKKIT:";

    private final JavaPlugin plugin;

    public CommandDeliveryService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<DeliveryResult> completeItemDelivery(@NonNull Delivery delivery, @NonNull Item item) {
        CompletableFuture<DeliveryResult> resultFuture = new CompletableFuture<>();

        //todo: complete command
        return CompletableFuture.completedFuture(DeliveryResult.INCOMPLETED);
    }

}
