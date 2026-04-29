package org.game24.marketsync.dao.impl;

import lombok.NonNull;
import org.game24.marketsync.dao.Database;
import org.game24.marketsync.dao.OrderDAO;
import org.game24.marketsync.model.Item;
import org.game24.marketsync.model.ItemType;
import org.game24.marketsync.model.Order;
import org.game24.marketsync.model.OrderStatus;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderSimpleDAO implements OrderDAO {

    private final Logger logger;

    private final Database database;

    public OrderSimpleDAO(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    @Override
    public @NonNull List<Order> findReadyOrdersForUser(@NonNull String username) {
        String sql = """
                SELECT po.id as order_id,
                       i.id as item_id,
                       i.item_type,
                       i.data,
                       i.amount
                    FROM mshop_player_orders po
                JOIN mshop_order_items oi on po.id = oi.order_id
                JOIN mshop_items i on oi.item_id = i.id
                WHERE user_name = ?
                AND status = 'PAID'
                """;


        Map<Long, List<Item>> orderToItems = new HashMap<>();
        Map<Long, Order.OrderBuilder> orderBuilders = new HashMap<>();
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long orderId = rs.getLong("order_id");

                    orderBuilders.computeIfAbsent(orderId, id ->
                            Order.builder()
                                    .id(id)
                                    .status(OrderStatus.PAID)
                                    .username(username)
                    );

                    // Собираем список предметов
                    orderToItems.computeIfAbsent(orderId, ignore -> new ArrayList<>())
                            .add(Item.builder()
                                    .id(rs.getLong("item_id"))
                                    .type(ItemType.valueOf(rs.getString("item_type"))) // enum безопаснее через valueOf
                                    .data(rs.getString("data"))
                                    .count(rs.getInt("amount"))
                                    .build());

                    orderToItems.computeIfAbsent(orderId, ignore -> new ArrayList<>())
                            .add(Item.builder()
                                    .id(rs.getLong("item_id"))
                                    .type(ItemType.valueOf(rs.getString("item_type"))) // enum безопаснее через valueOf
                                    .data(rs.getString("data"))
                                    .count(rs.getInt("amount"))
                                    .build());

                }
            }

            return orderBuilders.entrySet().stream()
                    .map(entry -> entry.getValue()
                            .items(orderToItems.getOrDefault(entry.getKey(), Collections.emptyList()))
                            .build())
                    .toList();

        } catch (SQLException e) {
            logger.error("Error on search for orders for user {}", username, e);
        }

        return Collections.emptyList();
    }

    @Override
    public void complete(long orderId) {

        String sql = """
                UPDATE mshop_player_orders
                SET status = 'COMPLETED'
                where id = ?
                """;

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error on order completion for id {}", orderId, e);
        }
    }
}
