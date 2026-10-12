package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

class TurnsActionTest {

    private final List<String> told = new ArrayList<>();
    private final VariableProvider variables = new VariableProvider();
    private final TurnsAction action = new TurnsAction(told::add, variables);

    @Test
    void execute_withSeveralTurns_printsThePluralForm() {
        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 5);

        action.execute();

        assertThat(told).containsExactly("You have taken 5 turns.");
    }

    @Test
    void execute_withExactlyOneTurn_printsTheSingularForm() {
        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 1);

        action.execute();

        assertThat(told).containsExactly("You have taken 1 turn.");
    }

    @Test
    void execute_withZeroTurns_printsThePluralForm() {
        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 0);

        action.execute();

        assertThat(told).containsExactly("You have taken 0 turns.");
    }

    @Test
    void execute_beforeTheVariableExists_countsZeroTurnsWithoutDefiningIt() {
        action.execute();

        assertThat(told).containsExactly("You have taken 0 turns.");
        assertThat(variables.isDefined(VariableProvider.TURNS_VARIABLE_NAME)).isFalse();
    }

    @Test
    void execute_followsTheValueTheAuthorSetTheVariableTo() {
        variables.define(new Variable(VariableProvider.TURNS_VARIABLE_NAME, 41));

        action.execute();

        assertThat(told).containsExactly("You have taken 41 turns.");
    }

    @Test
    void execute_buildsTheTextFromSm17ToSm20_soAnAdventureCanRewordIt() {
        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 2);
        Map<String, String> overrides = Map.of(
                SystemMessageKey.SM17.id(), "Thou hast made ",
                SystemMessageKey.SM18.id(), " move",
                SystemMessageKey.SM19.id(), "z",
                SystemMessageKey.SM20.id(), "!");

        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(overrides)) {
            action.execute();
            variables.set(VariableProvider.TURNS_VARIABLE_NAME, 1);
            action.execute();
        }

        assertThat(told).containsExactly("Thou hast made 2 movez!", "Thou hast made 1 move!");
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = action.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
