package org.game24.marketsync.game.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.game24.marketsync.config.AntiBrightnessConfig;
import org.game24.marketsync.game.antibrightness.AntiBrightnessTracker;
import org.game24.marketsync.game.antibrightness.AntiBrightnessTracker.SuspicionResult;

import java.time.Instant;

public class AntiBrightnessListener implements Listener {

    private static final int TICKS_IN_SECOND = 20;

    private final AntiBrightnessConfig config;

    private final AntiBrightnessTracker tracker;

    public AntiBrightnessListener(AntiBrightnessConfig config,
                                  AntiBrightnessTracker tracker) {
        this.config = config;
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.enabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (hasBypass(player) || !isSuspiciousLight(player, event.getBlock())) {
            return;
        }

        SuspicionResult result = tracker.record(player.getUniqueId(), Instant.now());
        if (!result.recorded()) {
            return;
        }

        if (result.warningRequired()) {
            player.sendMessage(config.warningMessage());
        }

        if (result.punishmentRequired()) {
            int durationTicks = config.punishmentDurationSeconds() * TICKS_IN_SECOND;
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, durationTicks, 0, false, false, true));
        }
    }

    private boolean hasBypass(Player player) {
        GameMode gameMode = player.getGameMode();
        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
            return true;
        }

        if (player.isOp()) {
            return true;
        }

        String bypassPermission = config.bypassPermission();
        if (bypassPermission != null && !bypassPermission.isBlank() && player.hasPermission(bypassPermission)) {
            return true;
        }

        return player.hasPotionEffect(PotionEffectType.NIGHT_VISION)
               || player.hasPotionEffect(PotionEffectType.CONDUIT_POWER);
    }

    private boolean isSuspiciousLight(Player player, Block brokenBlock) {
        int threshold = config.lightThreshold();
        if (player.getEyeLocation().getBlock().getLightLevel() > threshold) {
            return false;
        }

        if (player.getLocation().getBlock().getLightLevel() > threshold) {
            return false;
        }

        Block playerSideBlock = brokenBlock.getRelative(faceTowardPlayer(brokenBlock, player.getEyeLocation()));
        return !playerSideBlock.isPassable() || playerSideBlock.getLightLevel() <= threshold;
    }

    private BlockFace faceTowardPlayer(Block block, Location playerLocation) {
        double dx = playerLocation.getX() - (block.getX() + 0.5D);
        double dy = playerLocation.getY() - (block.getY() + 0.5D);
        double dz = playerLocation.getZ() - (block.getZ() + 0.5D);

        double absX = Math.abs(dx);
        double absY = Math.abs(dy);
        double absZ = Math.abs(dz);

        if (absY >= absX && absY >= absZ) {
            return dy >= 0 ? BlockFace.UP : BlockFace.DOWN;
        }

        if (absX >= absZ) {
            return dx >= 0 ? BlockFace.EAST : BlockFace.WEST;
        }

        return dz >= 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }
}
