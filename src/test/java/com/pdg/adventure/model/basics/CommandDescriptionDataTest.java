package com.pdg.adventure.model.basics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.model.Word;

class CommandDescriptionDataTest {
    CommandDescriptionData sut = new CommandDescriptionData();

    @Test
    public void getEmptyCommandSpec() throws Exception {
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
    public void createNewDataWithSpec() throws Exception {
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


    @Test
    public void createNewDataWithSpecEmpty() throws Exception {
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
    public void createNewDataWithSpecNull() throws Exception {
        // given
        String commandSpec = null;

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CommandDescriptionData(commandSpec);
        });

        // then
        assertEquals("Command spec must have 3 parts: " + commandSpec, exception.getMessage());
    }

    @Test
    public void createNewDataWithSpecWithEmptyAdjective() {
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
        assertEquals("", adjective.getText());
        assertEquals("raft", noun.getText());
    }

    @Test
    public void createNewDataWithSpecWithEmptyNoun() {
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
        assertEquals("", noun.getText());
    }

    @Test
    public void createNewDataWithSpecWithEmptyVerb() {
        // given
        String commandSpec = CommandDescription.COMMAND_SEPARATOR + "small" + CommandDescription.COMMAND_SEPARATOR +
                             "raft";
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData(commandSpec);

        // when
        Word verb = commandDescriptionData.getVerb();
        Word adjective = commandDescriptionData.getAdjective();
        Word noun = commandDescriptionData.getNoun();

        // then
        assertEquals("", verb.getText());
        assertEquals("small", adjective.getText());
        assertEquals("raft", noun.getText());
    }

    @Test
    public void createNewDataWithSpecWithEmptyAll() {
        // given
        String commandSpec = CommandDescription.COMMAND_SEPARATOR + CommandDescription.COMMAND_SEPARATOR +
                             CommandDescription.COMMAND_SEPARATOR;
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("|||");

        // when
        Word verb = commandDescriptionData.getVerb();
        Word adjective = commandDescriptionData.getAdjective();
        Word noun = commandDescriptionData.getNoun();

        // then
        assertEquals("", verb.getText());
        assertEquals("", adjective.getText());
        assertEquals("", noun.getText());
    }
}
