package com.pdg.adventure.model.basic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.model.Word;

class CommandDescriptionDataTest {
    CommandDescriptionData sut = new CommandDescriptionData();

    @Test
    void getEmptyCommandSpec() {
        // given

        // when
        String commandSpec = sut.getCommandSpecification();

        // then
        assertEquals(CommandDescription.COMMAND_SEPARATOR + CommandDescription.COMMAND_SEPARATOR, commandSpec);
    }

    @Test
    void getCommandSpecificaiton() {
        // given
        String commandSpec = "climb" + CommandDescription.COMMAND_SEPARATOR + "small" +
                             CommandDescription.COMMAND_SEPARATOR + "raft";

        // when
        sut.setCommandSpecification(commandSpec);

        // then
        assertEquals(commandSpec, sut.getCommandSpecification());
    }

    @Test
    void setCommandSpecification() {
        // given
        String commandSpec = "climb" + CommandDescription.COMMAND_SEPARATOR + "small" +
                             CommandDescription.COMMAND_SEPARATOR + "raft";

        // when
        sut.setCommandSpecification(commandSpec);

        // then
        assertEquals("climb", sut.getVerb().getText());
        assertEquals("small", sut.getAdjective().getText());
        assertEquals("raft", sut.getNoun().getText());
    }

    @Test
    void createNewDataWithSpec() {
        // given
        String commandSpec = "climb" + CommandDescription.COMMAND_SEPARATOR + "small" +
                             CommandDescription.COMMAND_SEPARATOR + "raft";
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData(commandSpec);

        // when
        Word verb = commandDescriptionData.getVerb();
        Word adjective = commandDescriptionData.getAdjective();
        Word noun = commandDescriptionData.getNoun();

        // then
        assertEquals("climb", verb.getText());
        assertEquals("small", adjective.getText());
        assertEquals("raft", noun.getText());
    }


    /*
    @Test
    void createNewDataWithSpecEmpty() throws Exception {
        // given
        String commandSpec = "";

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CommandDescriptionData(commandSpec);
        });

        // then
        assertEquals("Command spec must have 3 parts: " + commandSpec, exception.getMessage());
    }

    @Test
    void createNewDataWithSpecNull() throws Exception {
        // given
        String commandSpec = null;

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CommandDescriptionData(commandSpec);
        });

        // then
        assertEquals("Command spec must have 3 parts: " + commandSpec, exception.getMessage());
    }
*/

    @Test
    void createNewDataWithSpecWithEmptyAdjective() {
        // given
        String commandSpec = "climb" + CommandDescription.COMMAND_SEPARATOR + CommandDescription.COMMAND_SEPARATOR +
                             "raft";
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData(commandSpec);

        // when
        Word verb = commandDescriptionData.getVerb();
        Word adjective = commandDescriptionData.getAdjective();
        Word noun = commandDescriptionData.getNoun();

        // then
        assertEquals("climb", verb.getText());
        assertThat(adjective).isNull();
        assertEquals("raft", noun.getText());
    }

    @Test
    void createNewDataWithSpecWithEmptyNoun() {
        // given
        String commandSpec = "climb" + CommandDescription.COMMAND_SEPARATOR + "small" +
                             CommandDescription.COMMAND_SEPARATOR;
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData(commandSpec);

        // when
        Word verb = commandDescriptionData.getVerb();
        Word adjective = commandDescriptionData.getAdjective();
        Word noun = commandDescriptionData.getNoun();

        // then
        assertEquals("climb", verb.getText());
        assertEquals("small", adjective.getText());
        assertThat(noun).isNull();
    }

    @Test
    void createNewDataWithSpecWithEmptyVerb() {
        // given
        String commandSpec = CommandDescription.COMMAND_SEPARATOR + "small" + CommandDescription.COMMAND_SEPARATOR +
                             "raft";
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData(commandSpec);

        // when
        Word verb = commandDescriptionData.getVerb();
        Word adjective = commandDescriptionData.getAdjective();
        Word noun = commandDescriptionData.getNoun();

        // then
        assertThat(verb).isNull();
        assertEquals("small", adjective.getText());
        assertEquals("raft", noun.getText());
    }

    @Test
    void createNewDataWithSpecWithEmptyAll() {
        // given
        String commandSpec = CommandDescription.COMMAND_SEPARATOR + CommandDescription.COMMAND_SEPARATOR;
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData(commandSpec);

        // when
        Word verb = commandDescriptionData.getVerb();
        Word adjective = commandDescriptionData.getAdjective();
        Word noun = commandDescriptionData.getNoun();

        // then

        assertThat(verb).isNull();
        assertThat(adjective).isNull();
        assertThat(noun).isNull();
    }
}
