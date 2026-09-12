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

    @Test
    void newThing_emitsNoLightByDefault() {
        // Unlike Location (which defaults to lit, representing ambient room light), a plain Thing
        // - and so a plain Item, e.g. a key or a sword - shouldn't glow unless an author says so.
        Item item = new Item(new DescriptionProvider("key"), true);

        assertThat(item.getLight()).isZero();
    }

    @Test
    void setLight_makesAnItemEmitLight() {
        // The point of moving lumen up from Location to Thing: an Item (a torch, say) can now
        // carry its own light level too.
        Item torch = new Item(new DescriptionProvider("torch"), true);

        torch.setLight(80);

        assertThat(torch.getLight()).isEqualTo(80);
    }
}
