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
    void increment_addsOneToAnExistingVariable_andStartsAnUnknownOneAtOne() {
        VariableProvider provider = new VariableProvider();
        provider.set("score", 7);

        assertThat(provider.increment("score")).isEqualTo(8);
        assertThat(provider.increment("fresh")).isEqualTo(1);
        assertThat(provider.get("score").map(Variable::value)).contains(8);
        assertThat(provider.get("fresh").map(Variable::value)).contains(1);
    }

    @Test
    void get_ofAnUndefinedName_stillYieldsZeroWithoutDefiningIt() {
        VariableProvider provider = new VariableProvider();
        Optional<Variable> variable = provider.get("nope");
        assertThat(variable).isEmpty();
        assertThat(provider.isDefined("nope")).isFalse();
    }
}
