package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdventureDataVariablesTest {

    @Test
    void defineVariable_addsOnce_trimsTheName_andIgnoresBlank() {
        AdventureData adventure = new AdventureData();

        assertThat(adventure.getVariableData().addVariable(" Intoxication ")).isTrue();
        assertThat(adventure.getVariableData().addVariable("Intoxication")).isFalse();
        assertThat(adventure.getVariableData().addVariable("intoxication")).isTrue(); // names are case-sensitive
        assertThat(adventure.getVariableData().addVariable("  ")).isFalse();
        assertThat(adventure.getVariableData().addVariable(null)).isFalse();

        assertThat(adventure.variableNames()).containsExactly("Intoxication", "intoxication");
    }
}
