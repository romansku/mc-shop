package org.game24.marketsync.sync;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.game24.marketsync.service.PlayerRegistryService;
import org.slf4j.Logger;

public class PlayerBootstrapSync implements Runnable {

    private final PlayerRegistryService playerRegistryService;

    private final Logger logger;

    public PlayerBootstrapSync(PlayerRegistryService playerRegistryService, Logger logger) {
        this.playerRegistryService = playerRegistryService;
        this.logger = logger;
    }

    @Override
    public void run() {
        int scanned = 0;
        int inserted = 0;
        int skipped = 0;

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (!offlinePlayer.hasPlayedBefore()) {
                continue;
            }

            scanned++;
            String name = offlinePlayer.getName();
            if (name == null || name.isBlank()) {
                skipped++;
                continue;
            }

            if (playerRegistryService.saveIfAbsent(offlinePlayer.getUniqueId(), name)) {
                inserted++;
            } else {
                skipped++;
            }
        }

        logger.info("Player bootstrap sync finished: scanned={}, inserted={}, skipped={}",
                scanned, inserted, skipped);
    }
}
