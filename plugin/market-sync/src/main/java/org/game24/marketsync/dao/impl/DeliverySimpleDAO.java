package org.game24.marketsync.dao.impl;

import lombok.NonNull;
import org.game24.marketsync.dao.Database;
import org.game24.marketsync.dao.DeliveryDAO;
import org.game24.marketsync.model.Delivery;
import org.game24.marketsync.model.DeliveryResult;
import org.jspecify.annotations.Nullable;
import org.mariadb.jdbc.Statement;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DeliverySimpleDAO implements DeliveryDAO {

    private final Database database;

    private final Logger logger;

    public DeliverySimpleDAO(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    @Override
    public Delivery getByOrderAndItem(long orderId, long itemId, Long packId) {

        String sql = "select * from mshop_deliveries where order_id = ? and item_id = ?";
        if (packId != null) {
            sql += " and pack_id = ?";
        } else {
            sql += " and pack_id is null";
        }

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, orderId);
            ps.setLong(2, itemId);
            if (packId != null) {
                ps.setLong(3, packId);
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    long id = rs.getLong("id");
                    long order_id = rs.getLong("order_id");
                    long item_id = rs.getLong("item_id");
                    long pack_id = rs.getLong("pack_id");
                    String username = rs.getString("username");
                    DeliveryResult status = rs.getObject("status", DeliveryResult.class);
                    Instant attemptTime = rs.getTimestamp("attempt_time").toInstant();
                    int attempts = rs.getInt("attempts");
                    return Delivery.builder()
                            .id(id)
                            .orderId(order_id)
                            .itemId(item_id)
                            .packId(pack_id == 0 ? null : pack_id)
                            .username(username)
                            .status(status)
                            .attemptTime(attemptTime)
                            .attempts(attempts)
                            .build();
                }
            }

        } catch (SQLException e) {
            logger.error("getByOrderAndItem SQL Exception", e);
        }

        return null;
    }

    @Override
    public long save(@NonNull Delivery delivery) {

        String sql = """
                INSERT INTO mshop_deliveries
                (id, order_id, item_id, pack_id, username, status, attempt_time, attempts)
                VALUES (2, 1, 1, null, '111', 'FAILED', now(), 1)
                ON DUPLICATE KEY UPDATE id           = LAST_INSERT_ID(id),
                                        status       = VALUES(status),
                                        attempt_time = VALUES(attempt_time),
                                        attempts     = VALUES(attempts)
                """;

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. ID (если null, MariaDB создаст новый, если нет - обновит по нему)
            if (delivery.getId() != null && delivery.getId() > 0) {
                ps.setLong(1, delivery.getId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }

            ps.setLong(2, delivery.getOrderId());
            ps.setLong(3, delivery.getItemId());

            // 2. PackId (с учетом NULL)
            if (delivery.getPackId() != null) {
                ps.setLong(4, delivery.getPackId());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            ps.setString(5, delivery.getUsername());
            ps.setString(6, delivery.getStatus().name());
            ps.setTimestamp(7, Timestamp.from(delivery.getAttemptTime()));
            ps.setInt(8, delivery.getAttempts());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1); // Вернет существующий или новый ID
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving delivery", e);
        }
        return 0;
    }
}
