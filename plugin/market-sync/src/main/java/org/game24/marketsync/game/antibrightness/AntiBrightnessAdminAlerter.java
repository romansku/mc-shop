package org.game24.marketsync.game.antibrightness;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.game24.marketsync.config.AntiBrightnessConfig;
import org.game24.marketsync.game.antibrightness.AntiBrightnessTracker.SuspicionResult;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiBrightnessAdminAlerter {

    private final Logger logger;

    private volatile AntiBrightnessConfig config;

    private final Map<UUID, Long> lastAlertAtMs = new ConcurrentHashMap<>();

    public AntiBrightnessAdminAlerter(Logger logger, AntiBrightnessConfig config) {
        this.logger = logger;
        this.config = config;
    }

    public void updateConfig(AntiBrightnessConfig config) {
        this.config = config;
    }

    public void tryAlert(Player suspect,
                          SuspicionResult result,
                          AntiBrightnessLightSnapshot lights,
                          Location location) {
        AntiBrightnessConfig current = config;
        if (!current.adminAlertEnabled() || !result.recorded() || result.warningCount() != 1) {
            return;
        }

        UUID suspectId = suspect.getUniqueId();
        long nowMs = System.currentTimeMillis();
        long cooldownMs = current.adminAlertCooldownSeconds() * 1000L;
        Long lastAlert = lastAlertAtMs.get(suspectId);
        if (lastAlert != null && nowMs - lastAlert < cooldownMs) {
            return;
        }

        lastAlertAtMs.put(suspectId, nowMs);
        String message = formatMessage(current.adminAlertMessage(), suspect, result, lights, location);
        String consoleMessage = ChatColor.stripColor(message);

        logger.warn("[AntiBrightness] {}", consoleMessage);
        broadcastToAdmins(message, current.adminAlertPermission());
    }

    private void broadcastToAdmins(String message, String permission) {
        if (permission == null || permission.isBlank()) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(permission)) {
                online.sendMessage(message);
            }
        }
    }

    private String formatMessage(String template,
                                 Player suspect,
                                 SuspicionResult result,
                                 AntiBrightnessLightSnapshot lights,
                                 Location location) {
        String worldName = location.getWorld() == null ? "unknown" : location.getWorld().getName();
        String formatted = template
                .replace("{player}", suspect.getName())
                .replace("{world}", worldName)
                .replace("{x}", Integer.toString(location.getBlockX()))
                .replace("{y}", Integer.toString(location.getBlockY()))
                .replace("{z}", Integer.toString(location.getBlockZ()))
                .replace("{eye_light}", Integer.toString(lights.eyeLight()))
                .replace("{feet_light}", Integer.toString(lights.feetLight()))
                .replace("{block_light}", Integer.toString(lights.blockLight()))
                .replace("{count}", Integer.toString(result.warningCount()))
                .replace("{warning_window}", Integer.toString(result.warningWindowCount()))
                .replace("{punishment_window}", Integer.toString(result.punishmentWindowCount()));

        return ChatColor.translateAlternateColorCodes('&', formatted);
    }
}
