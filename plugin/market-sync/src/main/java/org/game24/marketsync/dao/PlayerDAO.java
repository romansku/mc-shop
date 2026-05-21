package org.game24.marketsync.dao;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface PlayerDAO {

    boolean saveBotIfAbsent(UUID uuid, String username);

    boolean markRegistered(UUID uuid, String username);

    Set<UUID> findExistingUuids(Collection<UUID> uuids);
}
