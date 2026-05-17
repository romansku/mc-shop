package org.game24.marketsync.game.placeholder;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.game24.marketsync.game.util.NickMeta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                    if (StringUtils.isNotBlank(rangPrefix)) {
                        rangPrefix = rangPrefix.trim() + RESET;
                    }

                    String userPrefix = StringUtils.EMPTY;
                    String prefixText = getOrEmpty(NickMeta.Key.PREFIX_TEXT);
                    if (StringUtils.isNotBlank(prefixText)) {
                        userPrefix = getOrEmpty(NickMeta.Key.PREFIX_COLOR) + prefixText + RESET;
                    }

                    String nickname = getOrEmpty(NickMeta.Key.NICK_COLOR) + username + RESET;

                    String userSuffix = StringUtils.EMPTY;
                    String suffixText = getOrEmpty(NickMeta.Key.SUFFIX_TEXT);
                    if (StringUtils.isNotBlank(suffixText)) {
                        userSuffix = getOrEmpty(NickMeta.Key.SUFFIX_COLOR) + suffixText + RESET;
                    }

                    tabPlaceholder = Stream.of(StringUtils.SPACE, rangPrefix, userPrefix, nickname, userSuffix)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(StringUtils.SPACE));
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
                    chatPlaceholder = tab + " <gray>»</gray> " + getOrEmpty(NickMeta.Key.CHAT_COLOR);
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
