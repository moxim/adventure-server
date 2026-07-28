package com.pdg.adventure.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.testhelper.TestSupporter;

class GenericCommandProviderDataTest {

    private final CommandProviderData sut = new CommandProviderData();
    private final String commandId = "c_id_1";
    private final VocabularyData vocabularyData = new VocabularyData();
    CommandDescriptionData cmdDesc = TestSupporter.createCommandDescriptionData(commandId, vocabularyData);

    @BeforeEach
    void setUp() throws Exception {
        sut.setId("id_1");
        sut.add(TestSupporter.createCommand(commandId, vocabularyData));
    }

    @Test
    void get() {
        assertThat(sut.getId()).isEqualTo("id_1");
        final Map<String, CommandChainData> availableCommands = sut.getAvailableCommands();
        assertThat(availableCommands.size()).isEqualTo(1);
        // The map is keyed by each chain's own stable id now, not the spec string - look the
        // chain up by matching its first command's trigger instead of a direct key lookup.
        CommandChainData chain = chainMatching(availableCommands, cmdDesc.getCommandSpecification());
        assertThat(chain).isNotNull();
        assertThat(chain.getCommands()).hasSize(1);
    }

    @Test
    void add() {
        String localCommandId = "c_id_2";
        sut.add(TestSupporter.createCommand(localCommandId, vocabularyData));
        final Map<String, CommandChainData> availableCommands = sut.getAvailableCommands();
        assertThat(availableCommands.size()).isEqualTo(2);
        final CommandDescriptionData commandDescriptionData =
                TestSupporter.createCommandDescriptionData(commandId, vocabularyData);
        assertThat(chainMatching(availableCommands, commandDescriptionData.getCommandSpecification())).isNotNull();
        CommandDescriptionData localCommandChain = TestSupporter.createCommandDescriptionData(localCommandId,
                                                                                              vocabularyData);

        CommandChainData localChain = chainMatching(availableCommands, localCommandChain.getCommandSpecification());
        assertThat(localChain).isNotNull();
        assertThat(localChain.getCommands()).hasSize(1);

        sut.add(TestSupporter.createCommand(localCommandId, vocabularyData));
        assertThat(availableCommands.size()).isEqualTo(2);

        assertThat(chainMatching(availableCommands, localCommandChain.getCommandSpecification()).getCommands())
                .hasSize(2);
    }

    private static CommandChainData chainMatching(Map<String, CommandChainData> availableCommands, String spec) {
        return availableCommands.values().stream()
                .filter(chain -> !chain.getCommands().isEmpty()
                        && chain.getCommands().getFirst().getCommandDescription().getCommandSpecification()
                                 .equals(spec))
                .findFirst()
                .orElse(null);
    }
}
