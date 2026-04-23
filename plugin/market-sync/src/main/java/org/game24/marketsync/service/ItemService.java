package org.game24.marketsync.service;

import lombok.NonNull;
import org.game24.marketsync.dao.ItemDAO;
import org.game24.marketsync.model.Item;

import java.util.List;
import java.util.Optional;

public class ItemService {

    private final ItemDAO itemDAO;

    public ItemService(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }


    public Optional<Item> find(long id) {
        return Optional.ofNullable(itemDAO.find(id));
    }

    public @NonNull List<Item> findChildren(long parentId) {
        return itemDAO.findChildren(parentId);
    }
}
