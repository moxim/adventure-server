package com.pdg.adventure.model;

import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.testhelper.TestSupporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CommandProviderDataTest {

    private CommandProviderData sut = new CommandProviderData();
    private String commandId = "c_id_1";
    private VocabularyData vocabularyData = new VocabularyData();
    CommandDescriptionData cmdDesc = TestSupporter.createCommandDescriptionData(commandId, vocabularyData);

    @BeforeEach
    public void setUp() throws Exception {
        sut.setId("id_1");
        sut.add(TestSupporter.createCommand(commandId, vocabularyData));
    }

    @Test
    void get() {
        assertThat(sut.getId()).isEqualTo("id_1");
        final Map<CommandDescriptionData, CommandChainData> availableCommands = sut.getAvailableCommands();
        assertThat(availableCommands.size()).isEqualTo(1);
        assertThat(availableCommands.get(cmdDesc)).isNotNull();
        assertThat(availableCommands.get(cmdDesc).getCommands()).hasSize(1);
    }

    @Test
    void add() {
        String localCommandId ="c_id_2";
        sut.add(TestSupporter.createCommand(localCommandId, vocabularyData));
        final Map<CommandDescriptionData, CommandChainData> availableCommands = sut.getAvailableCommands();
        assertThat(availableCommands.size()).isEqualTo(2);
        assertThat(availableCommands.get(TestSupporter.createCommandDescriptionData(commandId, vocabularyData))).isNotNull();
        CommandDescriptionData localCommandChain = TestSupporter.createCommandDescriptionData(localCommandId, vocabularyData);
        assertThat(availableCommands.get(localCommandChain)).isNotNull();
        assertThat(availableCommands.get(localCommandChain).getCommands()).hasSize(1);

        sut.add(TestSupporter.createCommand(localCommandId, vocabularyData));
        assertThat(availableCommands.size()).isEqualTo(2);

        assertThat(availableCommands.get(localCommandChain).getCommands()).hasSize(2);
    }
}
