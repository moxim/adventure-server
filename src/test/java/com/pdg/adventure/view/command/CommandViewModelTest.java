package com.pdg.adventure.view.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;

class CommandViewModelTest {

    private CommandDescriptionData commandDescriptionData;
    private Word verb;
    private Word adjective;
    private Word noun;

    @BeforeEach
    void setUp() {
        verb = new Word("get", Word.Type.VERB);
        adjective = new Word("rusty", Word.Type.ADJECTIVE);
        noun = new Word("key", Word.Type.NOUN);

        commandDescriptionData = new CommandDescriptionData();
        commandDescriptionData.setVerb(verb);
        commandDescriptionData.setAdjective(adjective);
        commandDescriptionData.setNoun(noun);
        commandDescriptionData.setCommandSpecification("get|rusty|key");
    }

    @Test
    void constructor_shouldInitializeFromCommandDescriptionData() {
        // When
        CommandViewModel viewModel = new CommandViewModel(commandDescriptionData);

        // Then
        assertThat(viewModel.getData()).isEqualTo(commandDescriptionData);
        assertThat(viewModel.getVerb().getText()).isEqualTo(verb.getText());
        assertThat(viewModel.getAdjective().getText()).isEqualTo(adjective.getText());
        assertThat(viewModel.getNoun().getText()).isEqualTo(noun.getText());
        assertThat(viewModel.getSpecification()).isEqualTo("get|rusty|key");
    }

    @Test
    void setVerb_shouldUpdateBothViewModelAndData() {
        // Given
        CommandViewModel viewModel = new CommandViewModel(commandDescriptionData);
        Word newVerb = new Word("take", Word.Type.VERB);

        // When
        viewModel.setVerb(newVerb);

        // Then
        assertThat(viewModel.getVerb()).isEqualTo(newVerb);
        assertThat(viewModel.getData().getVerb()).isEqualTo(newVerb);
    }

    @Test
    void setAdjective_shouldUpdateBothViewModelAndData() {
        // Given
        CommandViewModel viewModel = new CommandViewModel(commandDescriptionData);
        Word newAdjective = new Word("golden", Word.Type.ADJECTIVE);

        // When
        viewModel.setAdjective(newAdjective);

        // Then
        assertThat(viewModel.getAdjective()).isEqualTo(newAdjective);
        assertThat(viewModel.getData().getAdjective()).isEqualTo(newAdjective);
    }

    @Test
    void setNoun_shouldUpdateBothViewModelAndData() {
        // Given
        CommandViewModel viewModel = new CommandViewModel(commandDescriptionData);
        Word newNoun = new Word("sword", Word.Type.NOUN);

        // When
        viewModel.setNoun(newNoun);

        // Then
        assertThat(viewModel.getNoun()).isEqualTo(newNoun);
        assertThat(viewModel.getData().getNoun()).isEqualTo(newNoun);
    }

    @Test
    void setSpecification_shouldUpdateBothViewModelAndData() {
        // Given
        CommandViewModel viewModel = new CommandViewModel(commandDescriptionData);
        String newSpec = "open||door";

        // When
        viewModel.setSpecification(newSpec);

        // Then
        assertThat(viewModel.getSpecification()).isEqualTo(newSpec);
        assertThat(viewModel.getData().getCommandSpecification()).isEqualTo(newSpec);
    }

    @Test
    void constructor_shouldHandleNullWords() {
        // Given
        CommandDescriptionData dataWithNulls = new CommandDescriptionData();
        // Verb, adjective, noun are all null (not set)

        // When
        CommandViewModel viewModel = new CommandViewModel(dataWithNulls);

        // Then
        assertThat(viewModel.getVerb()).isNull();
        assertThat(viewModel.getAdjective()).isNull();
        assertThat(viewModel.getNoun()).isNull();
        assertThat(viewModel.getSpecification()).isEqualTo("||"); // Empty words result in separators only
    }

    @Test
    void setters_shouldWorkWithNullValues() {
        // Given
        CommandViewModel viewModel = new CommandViewModel(commandDescriptionData);

        // When
        viewModel.setVerb(null);
        viewModel.setAdjective(null);
        viewModel.setNoun(null);
        viewModel.setSpecification(null);

        // Then
        assertThat(viewModel.getVerb()).isNull();
        assertThat(viewModel.getAdjective()).isNull();
        assertThat(viewModel.getNoun()).isNull();
        // setSpecification(null) sets the local field to null
        assertThat(viewModel.getSpecification()).isNull();
        assertThat(viewModel.getData().getVerb()).isNull();
        assertThat(viewModel.getData().getAdjective()).isNull();
        assertThat(viewModel.getData().getNoun()).isNull();
        // getData().getCommandSpecification() constructs from null words, resulting in "||"
        assertThat(viewModel.getData().getCommandSpecification()).isEqualTo("||");
    }

    @Test
    void multipleSetters_shouldAllBeReflectedInData() {
        // Given
        CommandViewModel viewModel = new CommandViewModel(new CommandDescriptionData());
        Word newVerb = new Word("examine", Word.Type.VERB);
        Word newNoun = new Word("map", Word.Type.NOUN);
        String newSpec = "examine||map";

        // When
        viewModel.setVerb(newVerb);
        viewModel.setNoun(newNoun);
        viewModel.setSpecification(newSpec);

        // Then
        assertThat(viewModel.getData().getVerb().getText()).isEqualTo(newVerb.getText());
        assertThat(viewModel.getData().getNoun().getText()).isEqualTo(newNoun.getText());
        assertThat(viewModel.getData().getCommandSpecification()).isEqualTo(newSpec);
    }

    @Test
    void getData_shouldReturnSameInstancePassedToConstructor() {
        // Given
        CommandDescriptionData originalData = new CommandDescriptionData();

        // When
        CommandViewModel viewModel = new CommandViewModel(originalData);

        // Then
        assertThat(viewModel.getData()).isSameAs(originalData);
    }
}
