package com.pdg.adventure.server.tangible;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.testhelper.TestSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class ThingTest {
    private final Vocabulary vocabulary = new Vocabulary();

    {
        vocabulary.createNewWord("take", Word.Type.VERB);
    }

    private final Container pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);

    @Test
    void removeCommand() {
        // given
        DescriptionProvider thingDescription = new DescriptionProvider("thing");
        Item item = new Item(thingDescription, true);
        GenericCommandDescription commandDescription = new GenericCommandDescription("take");
        GenericCommand takeCommand = new GenericCommand(commandDescription, new MessageAction("Take-Command executed."));

        item.addCommand(takeCommand);

        // when
        assertThat(TestSupporter.applyCommandToBoolean(item, commandDescription)).isTrue();
        item.removeCommand(takeCommand);

        // then
        assertThat(TestSupporter.applyCommandToBoolean(item, commandDescription)).isFalse();
    }
}
