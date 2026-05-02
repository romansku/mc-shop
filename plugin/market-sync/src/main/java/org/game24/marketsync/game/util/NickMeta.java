package org.game24.marketsync.game.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public final class NickMeta {

    public static final BiMap<Key, String> META_KEYS = ImmutableBiMap.of(
            Key.RANG_PREFIX, "rang_prefix",
            Key.PREFIX_COLOR, "custom_prefix_color",
            Key.PREFIX_TEXT, "custom_prefix_text",
            Key.NICK_COLOR, "custom_nick_color",
            Key.SUFFIX_COLOR, "custom_suffix_color",
            Key.SUFFIX_TEXT, "custom_suffix_text",
            Key.CHAT_COLOR, "custom_chat_color");


    private NickMeta() {
    }

    public enum Key {
        RANG_PREFIX,
        PREFIX_COLOR,
        PREFIX_TEXT,
        NICK_COLOR,
        SUFFIX_COLOR,
        SUFFIX_TEXT,
        CHAT_COLOR,
    }
}
