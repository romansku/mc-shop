package org.game24.marketsync.dao.impl;

import org.game24.marketsync.dao.Database;
import org.game24.marketsync.dao.PlayerDAO;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

public class PlayerSimpleDAO implements PlayerDAO {

    private final Database database;

    private final Logger logger;

    public PlayerSimpleDAO(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    @Override
    public boolean saveIfAbsent(UUID uuid, String username) {
        if (uuid == null || username == null || username.isBlank()) {
            return false;
        }

        String sql = """
                INSERT IGNORE INTO mshop_players (uuid, username)
                VALUES (?, ?)
                """;

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ps.setString(2, username.toLowerCase(Locale.ROOT));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error saving player uuid={}, username={}", uuid, username, e);
            return false;
        }
    }
}
