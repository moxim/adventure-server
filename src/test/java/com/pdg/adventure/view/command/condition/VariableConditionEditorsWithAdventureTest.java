package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VariableData;
import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.model.condition.LessThanConditionData;
import com.pdg.adventure.model.condition.SameConditionData;

/** With an adventure at hand, variable conditions may only refer to variables some action defines. */
class VariableConditionEditorsWithAdventureTest {

    private AdventureData adventure;

    private static GreaterThanConditionData greater(String name) {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setVariableName(name);
        data.setValue(0);
        return data;
    }

    private static LessThanConditionData less(String name) {
        LessThanConditionData data = new LessThanConditionData();
        data.setVariableName(name);
        data.setValue(0);
        return data;
    }

    private static SameConditionData same(String one, String two) {
        SameConditionData data = new SameConditionData();
        data.setVariableNameOne(one);
        data.setVariableNameTwo(two);
        return data;
    }

    @BeforeEach
    void setUp() {
        adventure = new AdventureData();
        final VariableData variableData = adventure.getVariableData();
        variableData.addVariable("Intoxication", 0);
    }

    @Test
    void equals_withADefinedVariable_validates() {
        EqualsConditionEditor editor = new EqualsConditionEditor(new EqualsConditionData("Intoxication", 1), adventure);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
    }

    @Test
    void equals_withAnUndefinedVariable_orADifferentlyCasedOne_isRejected() {
        EqualsConditionEditor typo = new EqualsConditionEditor(new EqualsConditionData("intoxication", 1), adventure);
        typo.initialize();
        EqualsConditionEditor unknown = new EqualsConditionEditor(new EqualsConditionData("score", 1), adventure);
        unknown.initialize();

        assertThat(typo.validate()).isFalse();
        assertThat(unknown.validate()).isFalse();
    }

    @Test
    void greaterThanAndLessThan_applyTheSameRestriction() {
        GreaterThanConditionEditor ok = new GreaterThanConditionEditor(greater("Intoxication"), adventure);
        ok.initialize();
        LessThanConditionEditor bad = new LessThanConditionEditor(less("nope"), adventure);
        bad.initialize();

        assertThat(ok.validate()).isTrue();
        assertThat(bad.validate()).isFalse();
    }

    @Test
    void same_requiresBothVariablesToBeDefined() {
        SameConditionEditor ok = new SameConditionEditor(same("Intoxication", "VISITED"), adventure);
        ok.initialize();
        SameConditionEditor bad = new SameConditionEditor(same("Intoxication", "nope"), adventure);
        bad.initialize();

        assertThat(ok.validate()).isTrue();
        assertThat(bad.validate()).isFalse();
    }
}
