package org.game24.marketsync.dao;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface PlayerDAO {

    boolean saveIfAbsent(UUID uuid, String username);

    Set<UUID> findExistingUuids(Collection<UUID> uuids);
}
