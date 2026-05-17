package org.game24.marketsync.game.placeholder;

import org.game24.marketsync.game.util.NickMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class UserNickPlaceholderDTOTest {

    private static final String RESET = "<reset>";

    private UserNickPlaceholderDTO dto;

    @BeforeEach
    void setUp() {
        dto = new UserNickPlaceholderDTO("Steve");
    }

    @Test
    void showTabPlaceholder_withOnlyUsername_buildsNicknameWithReset() {
        String tab = dto.showTabPlaceholder();

        assertEquals("Steve" + RESET, tab);
    }

    @Test
    void showTabPlaceholder_withFullMeta_joinsRankPrefixPrefixNicknameSuffix() {
        dto.getValues().put(NickMeta.Key.RANG_PREFIX, "<gold>[VIP]</gold>");
        dto.getValues().put(NickMeta.Key.PREFIX_COLOR, "<red>");
        dto.getValues().put(NickMeta.Key.PREFIX_TEXT, "Admin");
        dto.getValues().put(NickMeta.Key.NICK_COLOR, "<green>");
        dto.getValues().put(NickMeta.Key.SUFFIX_COLOR, "<blue>");
        dto.getValues().put(NickMeta.Key.SUFFIX_TEXT, "Pro");

        String tab = dto.showTabPlaceholder();

        assertEquals(
                "<gold>[VIP]</gold>" + RESET + " "
                        + "<red>Admin" + RESET + " "
                        + "<green>Steve" + RESET + " "
                        + "<blue>Pro" + RESET,
                tab
        );
    }

    @Test
    void showTabPlaceholder_calledTwice_returnsCachedInstance() {
        String first = dto.showTabPlaceholder();
        String second = dto.showTabPlaceholder();

        assertSame(first, second);
    }

    @Test
    void showChatPlaceholder_withEmptyTabParts_stillAddsArrowAndChatColor() {
        dto.getValues().put(NickMeta.Key.CHAT_COLOR, "<yellow>");

        String chat = dto.showChatPlaceholder();

        assertEquals("Steve" + RESET + " <gray>»</gray> <yellow>", chat);
    }

    @Test
    void showChatPlaceholder_withTabMeta_prefixesTabBeforeArrow() {
        dto.getValues().put(NickMeta.Key.NICK_COLOR, "<white>");
        dto.getValues().put(NickMeta.Key.CHAT_COLOR, "<gray>");

        String chat = dto.showChatPlaceholder();

        assertEquals(
                "<white>Steve" + RESET + " <gray>»</gray> <gray>",
                chat
        );
    }

    @Test
    void showChatPlaceholder_calledTwice_returnsCachedInstance() {
        dto.getValues().put(NickMeta.Key.CHAT_COLOR, "<yellow>");

        String first = dto.showChatPlaceholder();
        String second = dto.showChatPlaceholder();

        assertSame(first, second);
    }

    @Test
    void showChatPlaceholder_reusesCachedTabPlaceholder() {
        dto.getValues().put(NickMeta.Key.CHAT_COLOR, "<yellow>");

        String tab = dto.showTabPlaceholder();
        String chat = dto.showChatPlaceholder();

        assertNotNull(tab);
        assertEquals("Steve" + RESET, tab);
        assertEquals("Steve" + RESET + " <gray>»</gray> <yellow>", chat);
    }

    @Test
    void reloadPlaceholders_afterMetaChange_rebuildsTabAndChat() {
        dto.getValues().put(NickMeta.Key.NICK_COLOR, "<white>");
        String tabBefore = dto.showTabPlaceholder();
        String chatBefore = dto.showChatPlaceholder();

        dto.getValues().put(NickMeta.Key.NICK_COLOR, "<red>");
        dto.reloadPlaceholders();

        String tabAfter = dto.showTabPlaceholder();
        String chatAfter = dto.showChatPlaceholder();

        assertNotEquals(tabBefore, tabAfter);
        assertNotEquals(chatBefore, chatAfter);
        assertEquals("<red>Steve" + RESET, tabAfter);
        assertEquals("<red>Steve" + RESET + " <gray>»</gray> ", chatAfter);
    }

    @Test
    void showTabPlaceholder_withBlankPrefixText_omitsUserPrefix() {
        dto.getValues().put(NickMeta.Key.PREFIX_COLOR, "<red>");
        dto.getValues().put(NickMeta.Key.PREFIX_TEXT, "   ");
        dto.getValues().put(NickMeta.Key.NICK_COLOR, "<green>");

        String tab = dto.showTabPlaceholder();

        assertEquals("<green>Steve" + RESET, tab);
    }

    @Test
    void showTabPlaceholder_withBlankRangPrefix_omitsRankSegment() {
        dto.getValues().put(NickMeta.Key.RANG_PREFIX, " ");
        dto.getValues().put(NickMeta.Key.NICK_COLOR, "<green>");

        String tab = dto.showTabPlaceholder();

        assertEquals("<green>Steve" + RESET, tab);
    }

}
