package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.DecrementVariableActionData;
import com.pdg.adventure.model.action.IncrementVariableActionData;
import com.pdg.adventure.model.action.SetVariableActionData;

/** Set defines variables (new names allowed); Increment and Decrement may only use defined ones. */
class VariableActionEditorsWithAdventureTest {

    private AdventureData adventure;

    @BeforeEach
    void setUp() {
        adventure = new AdventureData();
        adventure.getVariableData().addVariable("Intoxication");
    }

    @Test
    void set_acceptsABrandNewName() {
        SetVariableActionEditor editor = new SetVariableActionEditor(new SetVariableActionData("fresh", 1), adventure);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void increment_acceptsADefinedName_andRejectsAnUndefinedOne() {
        IncrementVariableActionData good = new IncrementVariableActionData();
        good.setName("Intoxication");
        good.setValue(1);
        IncrementVariableActionData bad = new IncrementVariableActionData();
        bad.setName("fresh");
        bad.setValue(1);
        IncrementVariableActionEditor goodEditor = new IncrementVariableActionEditor(good, adventure);
        goodEditor.initialize();
        IncrementVariableActionEditor badEditor = new IncrementVariableActionEditor(bad, adventure);
        badEditor.initialize();

        assertThat(goodEditor.validate()).isTrue();
        assertThat(badEditor.validate()).isFalse();
    }

    @Test
    void decrement_acceptsADefinedName_andRejectsAnUndefinedOne() {
        DecrementVariableActionData good = new DecrementVariableActionData();
        good.setName("Intoxication");
        good.setValue(1);
        DecrementVariableActionData bad = new DecrementVariableActionData();
        bad.setName("fresh");
        bad.setValue(1);
        DecrementVariableActionEditor goodEditor = new DecrementVariableActionEditor(good, adventure);
        goodEditor.initialize();
        DecrementVariableActionEditor badEditor = new DecrementVariableActionEditor(bad, adventure);
        badEditor.initialize();

        assertThat(goodEditor.validate()).isTrue();
        assertThat(badEditor.validate()).isFalse();
    }

    @Test
    void set_definesTheVariableImmediately_soAConditionEditorOpenedLaterCanUseIt() {
        SetVariableActionData data = new SetVariableActionData("Drunk", 1);
        SetVariableActionEditor editor = new SetVariableActionEditor(data, adventure);
        editor.initialize();
        assertThat(adventure.variableNames()).doesNotContain("Drunk"); // preset only, not yet entered

        editor.validate(); // what saving the command does
        assertThat(adventure.variableNames()).contains("Drunk");

        com.pdg.adventure.model.condition.EqualsConditionData condition =
                new com.pdg.adventure.model.condition.EqualsConditionData("Drunk", 1);
        com.pdg.adventure.view.command.condition.EqualsConditionEditor conditionEditor =
                new com.pdg.adventure.view.command.condition.EqualsConditionEditor(condition, adventure);
        conditionEditor.initialize();
        assertThat(conditionEditor.validate()).isTrue();
    }

    @Test
    void set_definesTheVariableTheMomentTheNameIsEntered() {
        SetVariableActionData data = new SetVariableActionData(null, null);
        SetVariableActionEditor editor = new SetVariableActionEditor(data, adventure);
        editor.initialize();

        editor.getChildren().filter(c -> c instanceof com.pdg.adventure.view.command.VariableNameSelector)
              .map(c -> (com.pdg.adventure.view.command.VariableNameSelector) c).findFirst().orElseThrow()
              .setValue("Fatigue");

        assertThat(adventure.variableNames()).contains("Fatigue");
        assertThat(data.getVariableName()).isEqualTo("Fatigue");
    }
}
