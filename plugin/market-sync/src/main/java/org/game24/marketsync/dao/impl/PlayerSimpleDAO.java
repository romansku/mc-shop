package org.game24.marketsync.dao.impl;

import org.game24.marketsync.dao.Database;
import org.game24.marketsync.dao.PlayerDAO;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerSimpleDAO implements PlayerDAO {

    public static final int EXISTING_UUID_BATCH_SIZE = 500;

    private final Database database;

    private final Logger logger;

    public PlayerSimpleDAO(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    @Override
    public boolean saveBotIfAbsent(UUID uuid, String username) {
        if (uuid == null || username == null || username.isBlank()) {
            return false;
        }

        String sql = """
                INSERT IGNORE INTO mshop_players (uuid, username, registered)
                VALUES (?, ?, 0)
                """;

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ps.setString(2, username.toLowerCase(Locale.ROOT));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error saving bot uuid={}, username={}", uuid, username, e);
            return false;
        }
    }

    @Override
    public boolean markRegistered(UUID uuid, String username) {
        if (uuid == null || username == null || username.isBlank()) {
            return false;
        }

        String sql = """
                INSERT INTO mshop_players (uuid, username, registered)
                VALUES (?, ?, 1)
                ON DUPLICATE KEY UPDATE registered = 1, username = VALUES(username)
                """;

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ps.setString(2, username.toLowerCase(Locale.ROOT));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking player registered uuid={}, username={}", uuid, username, e);
            return false;
        }
    }

    @Override
    public Set<UUID> findExistingUuids(Collection<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return Collections.emptySet();
        }

        List<UUID> uuidList = uuids instanceof List<UUID> list ? list : new ArrayList<>(uuids);
        Set<UUID> existing = new HashSet<>();
        for (int offset = 0; offset < uuidList.size(); offset += EXISTING_UUID_BATCH_SIZE) {
            existing.addAll(findExistingUuidsBatch(uuidList, offset));
        }
        return existing;
    }

    private Set<UUID> findExistingUuidsBatch(List<UUID> uuidList, int offset) {
        int end = Math.min(offset + PlayerSimpleDAO.EXISTING_UUID_BATCH_SIZE, uuidList.size());
        List<UUID> batch = uuidList.subList(offset, end);
        if (batch.isEmpty()) {
            return Collections.emptySet();
        }

        String placeholders = batch.stream().map(_ -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT uuid FROM mshop_players WHERE uuid IN (" + placeholders + ")";

        Set<UUID> existing = new HashSet<>();
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            for (UUID uuid : batch) {
                ps.setString(index++, uuid.toString());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existing.add(UUID.fromString(rs.getString("uuid")));
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading existing player uuids, batch offset={}", offset, e);
        }

        return existing;
    }
}
