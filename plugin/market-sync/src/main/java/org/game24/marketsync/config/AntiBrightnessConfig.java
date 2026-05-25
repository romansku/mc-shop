package org.game24.marketsync.config;

public record AntiBrightnessConfig(
        boolean enabled,
        int lightThreshold,
        long minIntervalMs,
        int warningThreshold,
        long warningWindowMs,
        int punishmentThreshold,
        long punishmentWindowMs,
        int punishmentDurationSeconds,
        int decayIntervalSeconds,
        String bypassPermission,
        String warningMessage
) {
}
