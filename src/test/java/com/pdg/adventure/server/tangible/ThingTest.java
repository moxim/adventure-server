package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThingTest {
    private final VariableProvider variableProvider = new VariableProvider();
    private final Vocabulary vocabulary = new Vocabulary();
    {
        vocabulary.addWord("take", Vocabulary.WordType.VERB);
    }

    @Test
    void removeCommand() {
        // given
        DescriptionProvider locationDescription = new DescriptionProvider("location");
        Location location = new Location(locationDescription);

        DescriptionProvider thingDescription = new DescriptionProvider("thing");
        Item item = new Item(thingDescription, true);
        CommandDescription commandDescription = new CommandDescription("take");
        GenericCommand takeCommand = new GenericCommand(commandDescription, new MessageAction("Take-Command executed."));

        item.addCommand(takeCommand);

        // when
        assertThat(item.applyCommand(commandDescription)).isEqualTo(true);
        item.removeCommand(takeCommand);

        // then
        assertThat(item.applyCommand(commandDescription)).isEqualTo(false);

    }
}
