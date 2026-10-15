package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.MesActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.MesAction;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.server.engine.FontMarkup;

@ExtendWith(MockitoExtension.class)
class MesActionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private MessagesHolder messagesHolder;

    private MesActionMapper mapper;

    @BeforeEach
    void setUp() {
        // must do this to avoid NPEs in mapper methods and to avoid Mockito strictness issues
        Mockito.lenient().when(adventureConfig.allMessages()).thenReturn(messagesHolder);
        mapper = new MesActionMapper(mapperSupporter, adventureConfig);
    }

    @Test
    void mapToDO_convertsMesActionToData() {
        // Given
        String messageText = "You enter a dark room.";
        MesAction action = new MesAction(messageText);

        // When
        MesActionData result = mapper.mapToDO(action);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessageId()).isEqualTo(messageText);
        assertThat(result).isInstanceOf(MesActionData.class);
    }

    @Test
    void mapToBO_convertsDataToMesActionWithLookup() {
        // Given
        String messageId = "dark_room_message";
        String expectedMessage = "You enter a dark room and hear strange noises.";
        MesActionData actionData = new MesActionData();
        actionData.setMessageId(messageId);

        when(messagesHolder.getMessage(messageId)).thenReturn(expectedMessage);

        // When
        MesAction result = mapper.mapToBO(actionData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo(expectedMessage);
        assertThat(result).isInstanceOf(MesAction.class);
    }

    @Test
    void mapToBO_fallsBackToMessageIdWhenNotFound() {
        // Given
        String messageId = "missing_message_id";
        MesActionData actionData = new MesActionData();
        actionData.setMessageId(messageId);

        when(messagesHolder.getMessage(messageId)).thenReturn(null);

        // When
        MesAction result = mapper.mapToBO(actionData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo(messageId);
        assertThat(result).isInstanceOf(MesAction.class);
    }

    @Test
    void mapToBO_carriesTheMessagesFontIntoTheAction() {
        MesActionData actionData = new MesActionData();
        actionData.setMessageId("note");
        when(messagesHolder.getMessage("note")).thenReturn("Meet me at midnight.");
        when(messagesHolder.getFont("note")).thenReturn(AdventureFont.SPECIAL_ELITE);

        MesAction result = mapper.mapToBO(actionData);

        assertThat(result.getFont()).isEqualTo(AdventureFont.SPECIAL_ELITE);
        assertThat(result.execute().getResultMessage())
                .isEqualTo(FontMarkup.wrap("Meet me at midnight.", AdventureFont.SPECIAL_ELITE));
    }

    @Test
    void mapToBO_treatsAMissingFontAsTheDefaultFont() {
        // the holder here is a mock, whose getFont returns null - as does any holder that knows no font
        MesActionData actionData = new MesActionData();
        actionData.setMessageId("note");
        when(messagesHolder.getMessage("note")).thenReturn("Plain text.");

        MesAction result = mapper.mapToBO(actionData);

        assertThat(result.getFont()).isEqualTo(AdventureFont.DEFAULT);
        assertThat(result.execute().getResultMessage()).isEqualTo("Plain text.");
    }
}
