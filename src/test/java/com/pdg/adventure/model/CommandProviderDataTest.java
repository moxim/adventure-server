package com.pdg.adventure.model;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.testhelper.TestSupporter;

class CommandProviderDataTest {

    private CommandProviderData sut = new CommandProviderData();
    private String commandId = "c_id_1";
    CommandDescriptionData cmdDesc = TestSupporter.createCommandDescriptionData(commandId);

    @BeforeEach
    public void setUp() throws Exception {
        sut.setId("id_1");
        sut.add(TestSupporter.createCommand(commandId));
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
        sut.add(TestSupporter.createCommand(localCommandId));
        final Map<CommandDescriptionData, CommandChainData> availableCommands = sut.getAvailableCommands();
        assertThat(availableCommands.size()).isEqualTo(2);
        assertThat(availableCommands.get(TestSupporter.createCommandDescriptionData(commandId))).isNotNull();
        CommandDescriptionData localCommandChain = TestSupporter.createCommandDescriptionData(localCommandId);
        assertThat(availableCommands.get(localCommandChain)).isNotNull();
        assertThat(availableCommands.get(localCommandChain).getCommands()).hasSize(1);

        sut.add(TestSupporter.createCommand(localCommandId));
        assertThat(availableCommands.size()).isEqualTo(2);

        assertThat(availableCommands.get(localCommandChain).getCommands()).hasSize(2);
    }
}
