package com.pdg.adventure.model.basics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.pdg.adventure.model.Word;

class CommandDescriptionDataTest {
    CommandDescriptionData sut = new CommandDescriptionData();

    @Test
    public void getEmptyCommandSpec() throws Exception {
        // given

        // when
        String commandSpec = sut.getCommandSpecification();

        // then
        assertEquals("||", commandSpec);
    }
    @Test
    void getCommandSpecificaiton() {
        // given
        String commandSpec = "climb|small|raft";

        // when
        sut.setCommandSpecification(commandSpec);

        // then
        assertEquals(commandSpec, sut.getCommandSpecification());
    }

    @Test
    void setCommandSpecification() {
        // given
        String commandSpec = "climb|small|raft";

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
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("climb|small|raft");

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
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("climb||raft");

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
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("climb|small|");

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
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("|small|raft");

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
