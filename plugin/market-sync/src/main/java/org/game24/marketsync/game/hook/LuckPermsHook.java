package org.game24.marketsync.game.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.plugin.java.JavaPlugin;
import org.game24.marketsync.model.DeliveryResult;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LuckPermsHook {

    private final JavaPlugin plugin;

    private volatile LuckPerms api;

    public LuckPermsHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    public CompletableFuture<DeliveryResult> addPermission(String username, String permission, long expireDays) {
        return this.addPermission(username, permission, expireDays, ChronoUnit.DAYS);
    }


    public CompletableFuture<DeliveryResult> addPermission(String username, String permission, long expire, ChronoUnit unit) {
        CompletableFuture<DeliveryResult> future = new CompletableFuture<>();
        Consumer<User> modifyUserConsumer = user -> modifyUserPermission(user, permission, expire, unit, future);
        return modifyUser(username, permission, modifyUserConsumer);
    }

    private @NonNull CompletableFuture<DeliveryResult> modifyUser(String username,
                                                                  String node,
                                                                  Consumer<User> modifyUserAction) {
        LuckPerms api = api();
        if (api == null) {
            plugin.getSLF4JLogger().error("LuckPerms API NOT INITIALIZED");
            return CompletableFuture.completedFuture(DeliveryResult.FAILED);
        }

        return api.getUserManager().lookupUniqueId(username)
                .thenCompose(uuid -> {
                    CompletableFuture<DeliveryResult> result = new CompletableFuture<>();

                    if (uuid == null) {
                        plugin.getSLF4JLogger().warn("Unknown player '{}' on this server", username);
                        result.complete(DeliveryResult.FAILED);
                        return result;
                    }

                    api.getUserManager().modifyUser(uuid, modifyUserAction);

                    return result;
                })
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(e -> {
                    plugin.getSLF4JLogger().warn("Permission {} was not added to player {}", node, username, e);
                    return DeliveryResult.INCOMPLETED;
                });
    }

    public CompletableFuture<DeliveryResult> addGroup(String username, String group, long days) {
        return addGroup(username, group, days, ChronoUnit.DAYS);
    }

    public CompletableFuture<DeliveryResult> addGroup(String username, String group, long days, ChronoUnit unit) {
        CompletableFuture<DeliveryResult> future = new CompletableFuture<>();
        Consumer<User> modifyUserConsumer = user -> modifyUserGroup(user, group, days, unit, future);
        return modifyUser(username, group, modifyUserConsumer);
    }

    private void modifyUserPermission(User user,
                                      String permission,
                                      long expire,
                                      ChronoUnit unit,
                                      CompletableFuture<DeliveryResult> result) {

        Instant now = Instant.now();
        Instant expiring = user.data().toCollection().stream()
                .filter(n -> n.getKey().equalsIgnoreCase(permission))
                .filter(Node::hasExpiry)
                .map(Node::getExpiry)
                .filter(Objects::nonNull)
                .filter(expiry -> expiry.isAfter(now))
                .map(instant -> instant.plus(expire, unit))
                .findFirst()
                .orElseGet(() -> now.plus(expire, unit));

        // Создаем узел разрешения
        Node node = Node.builder(permission)
                .expiry(expiring)
                .value(true)
                .build();

        // Удаляем узел у пользователя
        user.data().clear(n -> n.getKey().equalsIgnoreCase(permission));

        // Добавляем узел пользователю
        DataMutateResult mutateResult = user.data().add(node);

        if (mutateResult.wasSuccessful() || mutateResult == DataMutateResult.FAIL_ALREADY_HAS) {
            plugin.getSLF4JLogger().info("Permission {} was added to player {}", permission, user.getUsername());
            result.complete(DeliveryResult.COMPLETED);
            return;
        }

        plugin.getSLF4JLogger().warn("Permission {} was not added to player {}; result: {}",
                permission, user.getUsername(), mutateResult);
        result.complete(DeliveryResult.INCOMPLETED);
    }

    private void modifyUserGroup(User user,
                                 String group,
                                 long expire,
                                 ChronoUnit unit,
                                 CompletableFuture<DeliveryResult> result) {

        Optional<InheritanceNode> existingGroup = user.getNodes().stream()
                .filter(n -> n instanceof InheritanceNode)
                .map(InheritanceNode.class::cast)
                .filter(in -> in.getGroupName().equalsIgnoreCase(group))
                .findAny();

        Instant now = Instant.now();
        Instant expiring;
        if (existingGroup.isPresent()) {
            InheritanceNode in = existingGroup.get();
            if (!in.hasExpiry()) {
                plugin.getSLF4JLogger().warn("Group node is existing, but it is eternal; user {}, group {}",
                        user.getUsername(), group);
                result.complete(DeliveryResult.FAILED);
                return;
            }

            Instant currentExpiry = in.getExpiry();
            if (currentExpiry == null) {
                plugin.getSLF4JLogger().error("Must never here, expiry exists but null; user {}, group {}",
                        user.getUsername(), group);
                result.complete(DeliveryResult.FAILED);
                return;
            }

            if (currentExpiry.isAfter(now)) {
                expiring = currentExpiry.plus(expire, unit);
            } else {
                expiring = now.plus(expire, unit);
            }

            user.data().remove(in);
        } else {
            expiring = now.plus(expire, unit);
        }

        // Создаем узел разрешения
        InheritanceNode node = InheritanceNode.builder(group)
                .expiry(expiring)
                .value(true)
                .build();

        // Добавляем узел пользователю
        DataMutateResult mutateResult = user.data().add(node);

        if (mutateResult.wasSuccessful() || mutateResult == DataMutateResult.FAIL_ALREADY_HAS) {
            plugin.getSLF4JLogger().info("Group {} was added to player {}", group, user.getUsername());
            result.complete(DeliveryResult.COMPLETED);
            return;
        }

        plugin.getSLF4JLogger().warn("Group {} was not added to player {}; result: {}",
                group, user.getUsername(), mutateResult);
        result.complete(DeliveryResult.INCOMPLETED);
    }


    private LuckPerms api() {
        if (api == null) {
            synchronized (this) {
                if (api == null) {
                    api = LuckPermsProvider.get();
                }
            }
        }

        return api;
    }
}
