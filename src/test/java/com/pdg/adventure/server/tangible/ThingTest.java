package com.pdg.adventure.server.tangible;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.testhelper.TestSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

class ThingTest {
    private final VariableProvider variableProvider = new VariableProvider();
    private final Vocabulary vocabulary = new Vocabulary();
    {
        vocabulary.addWord("take", Vocabulary.WordType.VERB);
    }
    private Container pocket = new GenericContainer(new DescriptionProvider("your pocket"), 5);

    @Test
    void removeCommand() {
        // given
        DescriptionProvider locationDescription = new DescriptionProvider("location");
        Location location = new Location(locationDescription, new ContainerSupplier(pocket));

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
