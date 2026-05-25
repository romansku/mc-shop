package org.game24.marketsync.game.antibrightness;

import org.game24.marketsync.config.AntiBrightnessConfig;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class AntiBrightnessTracker {

    private volatile AntiBrightnessConfig config;

    private final ConcurrentHashMap<UUID, PlayerWarnings> warnings = new ConcurrentHashMap<>();

    public AntiBrightnessTracker(AntiBrightnessConfig config) {
        this.config = config;
    }

    public void updateConfig(AntiBrightnessConfig config) {
        this.config = config;
    }

    public SuspicionResult record(UUID uuid, Instant now) {
        long nowMs = now.toEpochMilli();
        AtomicReference<SuspicionResult> result = new AtomicReference<>(SuspicionResult.ignored());

        warnings.compute(uuid, (ignored, current) -> {
            PlayerWarnings playerWarnings = current == null ? new PlayerWarnings() : current;
            if (playerWarnings.lastWarningAtMs > 0 && nowMs - playerWarnings.lastWarningAtMs < config.minIntervalMs()) {
                return playerWarnings;
            }

            playerWarnings.lastWarningAtMs = nowMs;
            playerWarnings.warningCount++;
            playerWarnings.suspicionTimes.addLast(nowMs);
            playerWarnings.pruneOlderThan(nowMs - Math.max(config.warningWindowMs(), config.punishmentWindowMs()));

            int warningWindowCount = playerWarnings.countSince(nowMs - config.warningWindowMs());
            int punishmentWindowCount = playerWarnings.countSince(nowMs - config.punishmentWindowMs());
            boolean shouldWarn = !playerWarnings.warningSent
                    && warningWindowCount >= Math.max(1, config.warningThreshold());
            boolean shouldPunish = punishmentWindowCount >= Math.max(1, config.punishmentThreshold());

            if (shouldWarn) {
                playerWarnings.warningSent = true;
            }

            result.set(new SuspicionResult(
                    true,
                    shouldWarn,
                    shouldPunish,
                    playerWarnings.warningCount,
                    warningWindowCount,
                    punishmentWindowCount
            ));

            return playerWarnings;
        });

        return result.get();
    }

    public void decay(Instant now) {
        long nowMs = now.toEpochMilli();
        long maxWindowStartedAt = nowMs - Math.max(config.warningWindowMs(), config.punishmentWindowMs());

        warnings.forEach((uuid, ignored) -> warnings.computeIfPresent(uuid, (id, playerWarnings) -> {
            playerWarnings.pruneOlderThan(maxWindowStartedAt);
            if (nowMs - playerWarnings.lastWarningAtMs < config.decayIntervalSeconds() * 1000L) {
                return playerWarnings;
            }

            playerWarnings.warningCount = Math.max(0, playerWarnings.warningCount - 1);
            if (playerWarnings.warningCount == 0) {
                return null;
            }

            return playerWarnings;
        }));
    }

    public int trackedPlayers() {
        return warnings.size();
    }

    public record SuspicionResult(
            boolean recorded,
            boolean warningRequired,
            boolean punishmentRequired,
            int warningCount,
            int warningWindowCount,
            int punishmentWindowCount
    ) {

        private static SuspicionResult ignored() {
            return new SuspicionResult(false, false, false, 0, 0, 0);
        }
    }

    private static final class PlayerWarnings {

        private int warningCount;

        private long lastWarningAtMs;

        private boolean warningSent;

        private final Deque<Long> suspicionTimes = new ArrayDeque<>();

        private void pruneOlderThan(long oldestAcceptedAt) {
            while (!suspicionTimes.isEmpty() && suspicionTimes.peekFirst() < oldestAcceptedAt) {
                suspicionTimes.removeFirst();
            }
        }

        private int countSince(long startedAt) {
            int count = 0;
            for (Long suspicionAt : suspicionTimes) {
                if (suspicionAt >= startedAt) {
                    count++;
                }
            }
            return count;
        }
    }
}
