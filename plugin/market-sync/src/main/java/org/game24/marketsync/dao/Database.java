package org.game24.marketsync.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.config.MarketSyncConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


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

    public void init(JavaPlugin plugin) {
        try {
            initMigrationSchema(plugin);

            migration(plugin);

        } catch (Exception e) {
            throw new IllegalStateException("Error on init", e);
        }

    }

    private void initMigrationSchema(JavaPlugin plugin) throws IOException, SQLException {
        String migrationSchemaContent = loadResourceAsString("sql/migration.sql", plugin);

        try (Connection connection = getConnection()) {

            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {

                for (String expression : migrationSchemaContent.split(";")) {
                    String cleaned = expression.trim();
                    if (cleaned.isEmpty()) {
                        continue;
                    }

                    statement.addBatch(expression);
                }
                statement.executeBatch();
            }

            connection.commit();
        }
    }

    public void migration(JavaPlugin plugin) throws IOException {

        List<String> migrations = getMigrationFilesFromJar();
        try (Connection connection = getConnection()) {

            connection.setAutoCommit(false);

            try (Statement fileStatement = connection.createStatement()) {

                for (String filename : migrations) {
                    if (isMigrationApplied(connection, filename)) continue;

                    String content = loadResourceAsString(filename, plugin);
                    for (String expression : content.split(";\n")) {
                        String cleaned = expression.trim();
                        if (cleaned.isEmpty()) {
                            continue;
                        }
                        fileStatement.executeUpdate(expression);
                    }

                    try (PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO mshop_migration (name) VALUES (?)")) {
                        ps.setString(1, filename);
                        ps.executeUpdate();
                    }

                    connection.commit();
                    plugin.getSLF4JLogger().info("Successfully applied: {}", filename);
                }
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new IllegalStateException("Error on migration", e);
        }

    }

    private boolean isMigrationApplied(Connection connection, String filename) throws SQLException {
        String migSql = "select id from mshop_migration where name = ?";
        try (PreparedStatement migStatement = connection.prepareStatement(migSql)) {
            migStatement.setString(1, filename);
            ResultSet rs = migStatement.executeQuery();
            return rs.next();
        }
    }

    private String loadResourceAsString(String path, JavaPlugin plugin) throws IOException {
        try (var is = plugin.getResource(path)) {
            if (is == null) throw new IOException("Resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private List<String> getMigrationFilesFromJar() throws IOException {
        List<String> filenames = new ArrayList<>();
        URI uri;
        try {
            uri = Objects.requireNonNull(getClass().getClassLoader().getResource("sql/files")).toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid resource path", e);
        }

        // Если запускаем из JAR (в продакшене)
        if (uri.getScheme().equals("jar")) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                Path path = fileSystem.getPath("sql/files");
                try (var walk = Files.walk(path, 1)) {
                    walk.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".sql"))
                            .forEach(p -> filenames.add(p.toString()));
                }
            }
        } else {
            // Если запускаем из папки (например, при разработке в IDE)
            Path path = Paths.get(uri);
            try (var walk = Files.walk(path, 1)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".sql"))
                        .forEach(p -> filenames.add(p.getFileName().toString()));
            }
        }
        return filenames.stream().sorted().toList();
    }
}
