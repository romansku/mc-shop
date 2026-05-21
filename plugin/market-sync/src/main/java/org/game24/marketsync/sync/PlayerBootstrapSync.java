package org.game24.marketsync.sync;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.game24.marketsync.dao.impl.PlayerSimpleDAO;
import org.game24.marketsync.game.hook.AuthMeHook;
import org.game24.marketsync.service.PlayerRegistryService;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerBootstrapSync implements Runnable {

    private final PlayerRegistryService playerRegistryService;

    private final AuthMeHook authMeHook;

    private final Logger logger;

    public PlayerBootstrapSync(PlayerRegistryService playerRegistryService,
                               AuthMeHook authMeHook,
                               Logger logger) {
        this.playerRegistryService = playerRegistryService;
        this.authMeHook = authMeHook;
        this.logger = logger;
    }

    @Override
    public void run() {
        if (!authMeHook.isAvailable()) {
            logger.error("AuthMe is required for player bootstrap sync but API is not available");
            return;
        }

        logger.info("Starting to collect registered players");

        List<PlayerCandidate> candidates = collectCandidates();
        int candidatesCount = candidates.size();
        int alreadyInDb = 0;
        int notRegistered = 0;
        int inserted = 0;
        int skipped = 0;

        for (int offset = 0; offset < candidates.size(); offset += PlayerSimpleDAO.EXISTING_UUID_BATCH_SIZE) {
            int end = Math.min(offset + PlayerSimpleDAO.EXISTING_UUID_BATCH_SIZE, candidates.size());
            List<PlayerCandidate> batch = candidates.subList(offset, end);

            List<UUID> batchUuids = batch.stream().map(PlayerCandidate::uuid).toList();
            Set<UUID> existing = playerRegistryService.findExistingUuids(batchUuids);
            alreadyInDb += existing.size();

            for (PlayerCandidate candidate : batch) {
                if (existing.contains(candidate.uuid())) {
                    continue;
                }

                if (!authMeHook.isRegistered(candidate.username())) {
                    notRegistered++;
                    continue;
                }

                if (playerRegistryService.saveIfAbsent(candidate.uuid(), candidate.username())) {
                    inserted++;
                } else {
                    skipped++;
                }
            }
        }

        logger.info(
                "Player bootstrap sync finished: candidates={}, alreadyInDb={}, notRegistered={}, inserted={}, skipped={}",
                candidatesCount, alreadyInDb, notRegistered, inserted, skipped);
    }

    private List<PlayerCandidate> collectCandidates() {
        List<PlayerCandidate> candidates = new ArrayList<>();

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (!offlinePlayer.hasPlayedBefore()) {
                continue;
            }

            String name = offlinePlayer.getName();
            if (name == null || name.isBlank()) {
                continue;
            }

            candidates.add(new PlayerCandidate(offlinePlayer.getUniqueId(), name));
        }

        return candidates;
    }
}
