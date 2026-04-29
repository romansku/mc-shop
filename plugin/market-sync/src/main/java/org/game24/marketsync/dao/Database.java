package org.game24.marketsync.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.game24.marketsync.config.MarketSyncConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {

    private final HikariDataSource dataSource;

    public Database(MarketSyncConfig config) {

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(buildJdbcUrl(config));
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        hikariConfig.setMaximumPoolSize(config.getPoolMaxSize());
        hikariConfig.setMinimumIdle(config.getPoolMinSize());
        hikariConfig.setConnectionTimeout(5000);
        hikariConfig.setPoolName("MarketSync-Pool");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    private String buildJdbcUrl(MarketSyncConfig config) {
        String host = config.getHost();
        int port = config.getPort();
        String db = config.getDatabaseName();

        return "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false";
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
