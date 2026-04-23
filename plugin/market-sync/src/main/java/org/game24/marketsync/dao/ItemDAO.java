package org.game24.marketsync.dao;

import lombok.NonNull;
import org.game24.marketsync.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemDAO {

    @NonNull
    List<Item> findByIds(@NonNull Collection<Long> ids);

    Item find(long id);

    @NonNull
    List<Item> findChildren(long parentId);
}
