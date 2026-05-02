package org.game24.marketsync.game.placeholder;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.game24.marketsync.game.util.NickMeta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@RequiredArgsConstructor
public class UserNickPlaceholderDTO {

    private static final String RESET = "<reset>";

    private final Map<NickMeta.Key, String> values = new ConcurrentHashMap<>();

    private final String username;

    private volatile String tabPlaceholder;

    private volatile String chatPlaceholder;

    public String showTabPlaceholder() {
        if (tabPlaceholder == null) {
            synchronized (this) {
                if (tabPlaceholder == null) {
                    String rangPrefix = getOrEmpty(NickMeta.Key.RANG_PREFIX);

                    String userPrefix = getOrEmpty(NickMeta.Key.PREFIX_COLOR) + getOrEmpty(NickMeta.Key.PREFIX_TEXT);
                    if (StringUtils.isNotBlank(userPrefix)) {
                        userPrefix = StringUtils.SPACE + RESET + userPrefix;
                    }

                    String nickname = StringUtils.SPACE + RESET + getOrEmpty(NickMeta.Key.NICK_COLOR) + username;

                    String suffix = getOrEmpty(NickMeta.Key.SUFFIX_COLOR) + getOrEmpty(NickMeta.Key.SUFFIX_TEXT);
                    if (StringUtils.isNotBlank(suffix)) {
                        suffix = StringUtils.SPACE + RESET + suffix;
                    }

                    tabPlaceholder = rangPrefix +
                                     userPrefix +
                                     nickname +
                                     suffix;
                }
            }
        }

        return tabPlaceholder == null ? "" : tabPlaceholder;
    }

    public String showChatPlaceholder() {
        if (chatPlaceholder == null) {
            synchronized (this) {
                if (chatPlaceholder == null) {
                    String tab = showTabPlaceholder();
                    if (StringUtils.isNotBlank(tab)) {
                        tab = tab + StringUtils.SPACE + RESET;
                    }
                    chatPlaceholder = tab +
                                      "<gray>»</gray> " + getOrEmpty(NickMeta.Key.CHAT_COLOR);
                }
            }
        }

        return chatPlaceholder == null ? "" : chatPlaceholder;
    }

    public void reloadPlaceholders() {
        synchronized (this) {
            tabPlaceholder = null;
            chatPlaceholder = null;
            showChatPlaceholder();
        }
    }

    private String getOrEmpty(NickMeta.Key key) {
        return values.getOrDefault(key, StringUtils.EMPTY);
    }

}
