package org.game24.marketsync.game.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NicknamePlaceholderExpansion extends PlaceholderExpansion {

    private final NickPlaceholderData placeholderData;

    public NicknamePlaceholderExpansion(NickPlaceholderData placeholderData) {
        this.placeholderData = placeholderData;
    }


    @Override
    public @NotNull String getAuthor() {
        return "romansku";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mshop";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        UUID uuid = player.getUniqueId();
        return switch (params) {
            case "tab_user" -> {
                UserNickPlaceholderDTO user = placeholderData.getOffline(uuid);
                if (user == null) {
                    yield StringUtils.EMPTY;
                }

                yield user.showTabPlaceholder();
            }
            case "chat_user" -> {
                UserNickPlaceholderDTO user = placeholderData.getOffline(uuid);
                if (user == null) {
                    yield StringUtils.EMPTY;
                }

                yield user.showChatPlaceholder();
            }
            default -> StringUtils.EMPTY;
        };
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        UUID uuid = player.getUniqueId();
        return switch (params) {
            case "tab_user" -> {
                UserNickPlaceholderDTO user = placeholderData.get(uuid);
                if (user == null) {
                    yield StringUtils.EMPTY;
                }

                yield user.showTabPlaceholder();
            }
            case "chat_user" -> {
                UserNickPlaceholderDTO user = placeholderData.get(uuid);
                if (user == null) {
                    yield StringUtils.EMPTY;
                }

                yield user.showChatPlaceholder();
            }
            default -> StringUtils.EMPTY;
        };
    }
}
