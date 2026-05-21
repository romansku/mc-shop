package org.game24.marketsync.service;

import org.game24.marketsync.dao.PlayerDAO;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class PlayerRegistryService {

    private final PlayerDAO playerDAO;

    public PlayerRegistryService(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
    }

    public boolean saveBotIfAbsent(UUID uuid, String username) {
        return playerDAO.saveBotIfAbsent(uuid, username);
    }

    public boolean markRegistered(UUID uuid, String username) {
        return playerDAO.markRegistered(uuid, username);
    }

    public Set<UUID> findExistingUuids(Collection<UUID> uuids) {
        return playerDAO.findExistingUuids(uuids);
    }
}
