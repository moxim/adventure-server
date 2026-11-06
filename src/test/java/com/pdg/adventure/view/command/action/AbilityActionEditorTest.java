package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.textfield.IntegerField;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.AbilityActionData;

class AbilityActionEditorTest {

    private final AbilityActionData data = new AbilityActionData();

    private AbilityActionEditor editor() {
        AbilityActionEditor editor = new AbilityActionEditor(data, new AdventureData());
        editor.initialize();
        return editor;
    }

    private static List<IntegerField> fields(AbilityActionEditor anEditor) {
        return anEditor.getChildren().filter(IntegerField.class::isInstance).map(IntegerField.class::cast).toList();
    }

    @Test
    void hasAMaxWeightFieldAndAMaxNumberOfObjectsField_inThatOrder() {
        assertThat(fields(editor())).extracting(IntegerField::getLabel)
                                    .containsExactly("Max weight", "Max number of objects");
    }

    @Test
    void preselectsTheStoredValues() {
        data.setMaxWeight(20);
        data.setMaxItems(5);

        AbilityActionEditor editor = editor();

        assertThat(fields(editor)).extracting(IntegerField::getValue).containsExactly(20, 5);
        assertThat(editor.getActionSummary()).isEqualTo("weight 20, objects 5");
    }

    @Test
    void validate_isFalseWhileEitherValueIsMissing() {
        AbilityActionEditor editor = editor();
        assertThat(editor.validate()).isFalse();

        fields(editor).get(0).setValue(10);
        assertThat(editor.validate()).isFalse();

        fields(editor).get(1).setValue(3);
        assertThat(editor.validate()).isTrue();
    }

    @Test
    void validate_acceptsZero_butNotANegativeNumber() {
        AbilityActionEditor editor = editor();
        fields(editor).get(0).setValue(0);
        fields(editor).get(1).setValue(0);
        assertThat(editor.validate()).isTrue();

        fields(editor).get(0).setValue(-1);
        assertThat(editor.validate()).isFalse();
    }

    @Test
    void changingTheFields_updatesTheActionData() {
        AbilityActionEditor editor = editor();

        fields(editor).get(0).setValue(40);
        fields(editor).get(1).setValue(6);

        assertThat(data.getMaxWeight()).isEqualTo(40);
        assertThat(data.getMaxItems()).isEqualTo(6);
    }
}
