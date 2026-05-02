package org.game24.marketsync.game.placeholder;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.apache.commons.lang3.StringUtils;
import org.game24.marketsync.game.util.NickMeta;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.game24.marketsync.game.util.NickMeta.META_KEYS;

public class NickPlaceholderData {

    @Getter
    private final Map<UUID, UserNickPlaceholderDTO> userToPlaceholders = new ConcurrentHashMap<>();

    private final LoadingCache<UUID, UserNickPlaceholderDTO> offlineCache = CacheBuilder.newBuilder()
            .refreshAfterWrite(30, TimeUnit.SECONDS)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(CacheLoader.asyncReloading(
                    CacheLoader.from(this::loadOffline),
                    Executors.newVirtualThreadPerTaskExecutor()));


    public void put(UUID uuid, UserNickPlaceholderDTO dto) {
        if (uuid == null) {
            return;
        }

        if (dto == null) {
            userToPlaceholders.remove(uuid);
            return;
        }

        userToPlaceholders.put(uuid, dto);
    }

    public void remove(UUID uuid) {
        if (uuid == null) {
            return;
        }

        userToPlaceholders.remove(uuid);
    }

    public UserNickPlaceholderDTO get(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        return userToPlaceholders.get(uuid);
    }

    public UserNickPlaceholderDTO getOffline(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        return offlineCache.getUnchecked(uuid);
    }

    private UserNickPlaceholderDTO loadOffline(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().loadUser(uuid).join();
        if (user == null || user.getUsername() == null) {
            return null;
        }

        CachedDataManager cachedData = user.getCachedData();
        CachedMetaData metaData = cachedData.getMetaData();
        UserNickPlaceholderDTO dto = new UserNickPlaceholderDTO(user.getFriendlyName());
        for (NickMeta.Key key : META_KEYS.keySet()) {
            String metaKey = META_KEYS.get(key);
            String value = metaData.getMetaValue(metaKey);
            dto.getValues().put(key, value == null ? StringUtils.EMPTY : value);
        }

        dto.reloadPlaceholders();
        return dto;
    }


}
