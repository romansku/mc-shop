package org.game24.marketsync.game.listener;

import fr.xephi.authme.events.RegisterEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.game24.marketsync.service.PlayerRegistryService;

public class PlayerRegistryListener implements Listener {

    private final PlayerRegistryService playerRegistryService;

    public PlayerRegistryListener(PlayerRegistryService playerRegistryService) {
        this.playerRegistryService = playerRegistryService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegister(RegisterEvent event) {
        Player player = event.getPlayer();
        playerRegistryService.markRegistered(player.getUniqueId(), player.getName());
    }
}
