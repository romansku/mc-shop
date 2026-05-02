package org.game24.marketsync.game.listener;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.game.placeholder.NickPlaceholderData;
import org.game24.marketsync.game.placeholder.UserNickPlaceholderDTO;
import org.game24.marketsync.game.util.NickMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.game24.marketsync.game.util.NickMeta.META_KEYS;

public class NicknameChangeListener implements Listener {


    private final JavaPlugin plugin;

    private final List<EventSubscription<? extends LuckPermsEvent>> subscriptions = new ArrayList<>();

    private final NickPlaceholderData nickPlaceholderData;

    public NicknameChangeListener(JavaPlugin plugin, NickPlaceholderData nickPlaceholderData) {
        this.plugin = plugin;
        this.nickPlaceholderData = nickPlaceholderData;
    }


    public void init() {
        EventSubscription<NodeAddEvent> nodeAddSubscription = subscribeOnEvent(NodeAddEvent.class, this::onAddNode);
        subscriptions.add(nodeAddSubscription);

        EventSubscription<NodeRemoveEvent> nodeRemoveSubscription = subscribeOnEvent(NodeRemoveEvent.class, this::onRemoveNode);
        subscriptions.add(nodeRemoveSubscription);
    }

    private void onAddNode(NodeAddEvent e) {
        Node node = e.getNode();
        onNodeMutateEvent(node, e.getTarget());
    }

    private void onRemoveNode(NodeRemoveEvent e) {
        Node node = e.getNode();
        onNodeMutateEvent(node, e.getTarget());
    }

    private void onNodeMutateEvent(Node node, PermissionHolder holder) {
        if (node.getType() != NodeType.META) {
            return;
        }

        if (holder instanceof User user) {
            updateValueForUser(user, node);

        } else if (holder instanceof Group group) {
            LuckPerms api = LuckPermsProvider.get();
            UserManager um = api.getUserManager();

            Map<UUID, UserNickPlaceholderDTO> placeholders = nickPlaceholderData.getUserToPlaceholders();
            for (UUID uuid : placeholders.keySet()) {
                User loadedUser = um.getUser(uuid);
                if (loadedUser == null) {
                    continue;
                }

                String groupName = loadedUser.getPrimaryGroup();
                if (!group.getName().equals(groupName)) {
                    continue;
                }

                updateValueForUser(loadedUser, node);
            }
        }
    }

    private void updateValueForUser(User user, Node node) {
        UUID uuid = user.getUniqueId();

        UserNickPlaceholderDTO placeholderDTO = nickPlaceholderData.get(uuid);
        if (placeholderDTO == null) {
            // don't touch offline users
            return;
        }

        String key = node.getKey();
        NickMeta.Key metaKey = META_KEYS.inverse().get(key);
        if (metaKey == null) {
            // no nickname placeholders
            return;
        }

        String value = user.getCachedData().getMetaData().getMetaValue(key);
        placeholderDTO.getValues().put(metaKey, value == null ? StringUtils.EMPTY : value);
        placeholderDTO.reloadPlaceholders();
    }


    private <E extends LuckPermsEvent> EventSubscription<E> subscribeOnEvent(
            Class<E> eventType,
            Consumer<E> eventConsumer) {
        LuckPerms api = LuckPermsProvider.get();

        return api.getEventBus().subscribe(plugin, eventType, eventConsumer);
    }

    @EventHandler
    public void onUserJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        LuckPerms api = LuckPermsProvider.get();

        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            plugin.getSLF4JLogger().warn("No user fount on PlayerJoinEvent; username: {}", player.getName());
            return;
        }

        UserNickPlaceholderDTO dto = new UserNickPlaceholderDTO(user.getFriendlyName());
        CachedDataManager cachedData = user.getCachedData();
        CachedMetaData metaData = cachedData.getMetaData();
        for (NickMeta.Key key : META_KEYS.keySet()) {
            String metaKey = META_KEYS.get(key);
            String value = metaData.getMetaValue(metaKey);
            dto.getValues().put(key, value == null ? StringUtils.EMPTY : value);
        }

        nickPlaceholderData.put(player.getUniqueId(), dto);
    }


    @EventHandler
    public void onUserLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        nickPlaceholderData.remove(uuid);
    }

    public void destroy() {
        subscriptions.forEach(EventSubscription::close);
    }
}
