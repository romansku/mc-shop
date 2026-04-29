package org.game24.marketsync.dao.impl;

import lombok.NonNull;
import org.game24.marketsync.dao.Database;
import org.game24.marketsync.dao.ItemDAO;
import org.game24.marketsync.model.Item;
import org.game24.marketsync.model.ItemType;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ItemSimpleDAO implements ItemDAO {

    private final Database database;

    private final Logger logger;

    public ItemSimpleDAO(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    @Override
    public Item find(long id) {
        String sql = "select * from mshop_items where id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapItem(rs);

            }

        } catch (SQLException e) {
            logger.error("Error on search for item by id {}", id, e);
        }

        return null;
    }

    private Item mapItem(ResultSet rs) throws SQLException {
        return Item.builder()
                .id(rs.getLong("id"))
                .type(ItemType.valueOf(rs.getString("item_type")))
                .data(rs.getString("data"))
                .count(rs.getInt("amount"))
                .build();
    }

    @Override
    public @NonNull List<Item> findChildren(long parentId) {

        String sql = """
                select
                    item.id,
                    item.item_type,
                    item.data,
                    item.amount
                    from mshop_item_packs pack
                join mshop_items item on pack.child_item_id = item.id
                where parent_item_id = ?""";

        List<Item> items = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Item item = mapItem(rs);
                items.add(item);
            }

        } catch (SQLException e) {
            logger.error("Error on search children items for parent {}", parentId, e);
        }

        return items;
    }
}
