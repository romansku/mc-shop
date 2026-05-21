package org.game24.marketsync.game.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.game24.marketsync.service.PlayerRegistryService;

public class PlayerRegistryListener implements Listener {

    private final PlayerRegistryService playerRegistryService;

    public PlayerRegistryListener(PlayerRegistryService playerRegistryService) {
        this.playerRegistryService = playerRegistryService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        playerRegistryService.saveIfAbsent(event.getUniqueId(), event.getName());
    }
}
