package org.game24.marketsync.dao;

import java.util.UUID;

public interface PlayerDAO {

    boolean saveIfAbsent(UUID uuid, String username);
}
