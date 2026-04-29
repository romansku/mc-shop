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

}
