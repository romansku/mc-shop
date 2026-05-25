package org.game24.marketsync.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MarketSyncConfig {

    private final FileConfiguration config;

    public MarketSyncConfig(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public String getHost() {
        return this.config.getString("database.host");
    }

    public int getPort() {
        return this.config.getInt("database.port");
    }

    public String getUsername() {
        return this.config.getString("database.username");
    }

    public String getPassword() {
        return this.config.getString("database.password");
    }

    public String getDatabaseName() {
        return this.config.getString("database.db");
    }

    public int getPoolMaxSize() {
        return this.config.getInt("database.pool.max");
    }

    public int getPoolMinSize() {
        return this.config.getInt("database.pool.min");
    }

    public int getJobDelay() {
        return this.config.getInt("job.delay-minutes");
    }

    public AntiBrightnessConfig getAntiBrightnessConfig() {
        return new AntiBrightnessConfig(
                this.config.getBoolean("anti-brightness.enabled", true),
                this.config.getInt("anti-brightness.light-threshold", 1),
                this.config.getLong("anti-brightness.min-interval-ms", 2000),
                this.config.getInt("anti-brightness.warning-threshold", 3),
                this.config.getLong("anti-brightness.warning-window-ms", 10000),
                this.config.getInt("anti-brightness.punishment-threshold", 4),
                this.config.getLong("anti-brightness.punishment-window-ms", 15000),
                this.config.getInt("anti-brightness.punishment-duration-seconds", 10),
                this.config.getInt("anti-brightness.decay-interval-seconds", 10),
                this.config.getString("anti-brightness.bypass-permission", "marketsync.antibrightness.bypass"),
                this.config.getString(
                        "anti-brightness.warning-message",
                        "Всевидящее око чует запретную магию в этой тьме. Оставь ее, иначе мрак сам взыщет с нарушителя."
                )
        );
    }
}
