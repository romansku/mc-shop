package org.game24.marketsync.dao;

import lombok.NonNull;
import org.game24.marketsync.model.Item;

import java.util.List;

public interface ItemDAO {

    Item find(long id);

    @NonNull
    List<Item> findChildren(long parentId);
}
