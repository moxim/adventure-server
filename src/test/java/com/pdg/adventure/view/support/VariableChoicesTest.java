package com.pdg.adventure.view.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VariableData;

class VariableChoicesTest {

    @Test
    void of_listsTheAdventuresVariablesPlusVisited_sortedAndLive() {
        AdventureData adventure = new AdventureData();
        final VariableData variableData = adventure.getVariableData();
        variableData.addVariable("zeta", 22);
        variableData.addVariable("Alpha", 1);

        assertThat(VariableChoices.of(adventure)).containsExactly("Alpha", "VISITED", "zeta");

        variableData.addVariable("beta", 2);
        assertThat(VariableChoices.of(adventure)).containsExactly("Alpha", "beta", "VISITED", "zeta");
        assertThat(VariableChoices.of(null)).containsExactly("VISITED");
    }
}
