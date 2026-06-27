package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.server.exception.QuitException;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@ExtendWith(MockitoExtension.class)
class QuitActionTest {

    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_throwsQuitException() {
        assertThatThrownBy(() -> new QuitAction(messagesHolder).execute())
                .isInstanceOf(QuitException.class);
    }
}
