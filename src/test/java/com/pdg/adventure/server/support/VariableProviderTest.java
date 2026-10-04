package com.pdg.adventure.server.support;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VariableProviderTest {

    @Test
    void define_registersAVariableWithItsValue_andClearForgetsAll() {
        VariableProvider provider = new VariableProvider();
        provider.define(new Variable("score", 7));

        assertThat(provider.isDefined("score")).isTrue();
        Optional<Variable> variable = provider.get("score");
        if (variable.isPresent()) {
            assertThat(variable.get().name()).isEqualTo("score");
            assertThat(variable.get().value()).isEqualTo(7);
        } else {
            throw new AssertionError("Variable 'score' should be defined.");
        }
        assertThat(provider.getAll()).extracting(Variable::name).containsExactly("score");

        provider.clear();
        assertThat(provider.isDefined("score")).isFalse();
        assertThat(provider.getAll()).isEmpty();
    }

    @Test
    void get_ofAnUndefinedName_stillYieldsZeroWithoutDefiningIt() {
        VariableProvider provider = new VariableProvider();
        Optional<Variable> variable = provider.get("nope");
        assertThat(variable).isEmpty();
        assertThat(provider.isDefined("nope")).isFalse();
    }
}
