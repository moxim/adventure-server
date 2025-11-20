package com.pdg.adventure.view.command;

import org.junit.jupiter.api.Test;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;

import static org.assertj.core.api.Assertions.assertThat;

class CommandDescriptionAdapterTest {

    @Test
    void constructor_withCommandDescriptionData_shouldWrapCorrectly() {
        // Given
        Word verb = new Word("get", Word.Type.VERB);
        Word adjective = new Word("rusty", Word.Type.ADJECTIVE);
        Word noun = new Word("key", Word.Type.NOUN);
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("get|rusty|key");
        commandDescriptionData.setVerb(verb);
        commandDescriptionData.setAdjective(adjective);
        commandDescriptionData.setNoun(noun);

        // When
        CommandDescriptionAdapter adapter = new CommandDescriptionAdapter(commandDescriptionData);

        // Then
        assertThat(adapter.getCommandDescription()).isSameAs(commandDescriptionData);
        assertThat(adapter.getVerb()).isEqualTo("get");
        assertThat(adapter.getAdjective()).isEqualTo("rusty");
        assertThat(adapter.getNoun()).isEqualTo("key");
        assertThat(adapter.getShortDescription()).isEqualTo("get|rusty|key");
    }

    @Test
    void constructor_withCommandDescriptionData_shouldHandleNullWords() {
        // Given
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("look||");
        commandDescriptionData.setVerb(new Word("look", Word.Type.VERB));
        commandDescriptionData.setAdjective(null);
        commandDescriptionData.setNoun(null);

        // When
        CommandDescriptionAdapter adapter = new CommandDescriptionAdapter(commandDescriptionData);

        // Then
        assertThat(adapter.getVerb()).isEqualTo("look");
        assertThat(adapter.getAdjective()).isEmpty();
        assertThat(adapter.getNoun()).isEmpty();
    }

    @Test
    void constructor_withCommandSpecificationString_shouldCreateCommandDescriptionData() {
        // Given
        String commandSpec = "take|golden|sword";

        // When
        CommandDescriptionAdapter adapter = new CommandDescriptionAdapter(commandSpec);

        // Then
        assertThat(adapter.getCommandDescription()).isNotNull();
        assertThat(adapter.getCommandDescription().getCommandSpecification()).isEqualTo(commandSpec);
        assertThat(adapter.getShortDescription()).isEqualTo(commandSpec);
    }

    @Test
    void describableMethods_shouldReturnExpectedValues() {
        // Given
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("get|rusty|key");
        commandDescriptionData.setId("test-id-123");
        CommandDescriptionAdapter adapter = new CommandDescriptionAdapter(commandDescriptionData);

        // When & Then
        assertThat(adapter.getBasicDescription()).isNull();
        assertThat(adapter.getEnrichedBasicDescription()).isNull();
        assertThat(adapter.getLongDescription()).isNull();
        assertThat(adapter.getEnrichedShortDescription()).isNull();
        assertThat(adapter.getId()).isEqualTo("test-id-123");
    }

    @Test
    void setId_shouldUpdateCommandDescriptionDataId() {
        // Given
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData("look||");
        CommandDescriptionAdapter adapter = new CommandDescriptionAdapter(commandDescriptionData);

        // When
        adapter.setId("new-id-456");

        // Then
        assertThat(adapter.getId()).isEqualTo("new-id-456");
        assertThat(commandDescriptionData.getId()).isEqualTo("new-id-456");
    }
}
