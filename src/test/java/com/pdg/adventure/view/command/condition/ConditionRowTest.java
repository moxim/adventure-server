package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

class ConditionRowTest {

    private ConditionEditorComponent editor;

    @BeforeEach
    void setUp() {
        EqualsConditionData eqData = new EqualsConditionData();
        editor = new EqualsConditionEditor(eqData);
        editor.initialize();
    }

    @Test
    void toConditionData_withNegateUnchecked_returnsLeafData() {
        ConditionRow row = new ConditionRow(editor, false);
        PreConditionData result = row.toConditionData();
        assertThat(result).isSameAs(editor.getConditionData());
        assertThat(result).isNotInstanceOf(NotConditionData.class);
    }

    @Test
    void toConditionData_withNegateChecked_returnsNotConditionDataWrappingLeaf() {
        ConditionRow row = new ConditionRow(editor, true);
        PreConditionData result = row.toConditionData();
        assertThat(result).isInstanceOf(NotConditionData.class);
        assertThat(((NotConditionData) result).getPreCondition()).isSameAs(editor.getConditionData());
    }

    @Test
    void constructor_buildsUI() {
        ConditionRow row = new ConditionRow(editor, false);
        assertThat(row.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void setOnRemove_acceptsCallback() {
        ConditionRow row = new ConditionRow(editor, false);
        row.setOnRemove(() -> {});
        assertThat(row).isNotNull();
    }

    @Test
    void constructor_withFilledEditor_summaryIncludesContent() {
        EqualsConditionData eqData = new EqualsConditionData();
        eqData.setVariableName("score");
        eqData.setValue("10");
        ConditionEditorComponent filledEditor = new EqualsConditionEditor(eqData);
        filledEditor.initialize();

        ConditionRow row = new ConditionRow(filledEditor, false);

        assertThat(row.getSummaryText()).contains("Equals").contains("score");
    }

    @Test
    void constructor_withEmptyEditor_summaryShowsTypeNameAndNone() {
        ConditionRow row = new ConditionRow(editor, false);
        assertThat(row.getSummaryText()).isEqualTo("Equals: (none)");
    }
}
