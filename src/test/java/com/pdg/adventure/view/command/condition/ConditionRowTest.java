package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
    void setOnChange_firesWhenNegateCheckboxToggles() {
        ConditionRow row = new ConditionRow(editor, false);
        boolean[] fired = {false};
        row.setOnChange(() -> fired[0] = true);

        findNegateCheckbox(row).setValue(true);

        assertThat(fired[0]).isTrue();
    }

    @Test
    void upButton_firesOnMoveUp() {
        ConditionRow row = new ConditionRow(editor, false);
        boolean[] fired = {false};
        row.setOnMoveUp(() -> fired[0] = true);

        findButton(row, "Up").click();

        assertThat(fired[0]).isTrue();
    }

    @Test
    void downButton_firesOnMoveDown() {
        ConditionRow row = new ConditionRow(editor, false);
        boolean[] fired = {false};
        row.setOnMoveDown(() -> fired[0] = true);

        findButton(row, "Down").click();

        assertThat(fired[0]).isTrue();
    }

    // Details wraps content in a Div: ConditionRow -> Div -> HorizontalLayout(controls) -> {Checkbox, Buttons}
    private HorizontalLayout controls(ConditionRow row) {
        return row.getChildren()
                .filter(c -> c instanceof Div)
                .findFirst()
                .flatMap(div -> ((Div) div).getChildren()
                        .filter(c -> c instanceof HorizontalLayout)
                        .findFirst())
                .map(c -> (HorizontalLayout) c)
                .orElseThrow();
    }

    private Checkbox findNegateCheckbox(ConditionRow row) {
        return controls(row).getChildren()
                .filter(c -> c instanceof Checkbox)
                .map(c -> (Checkbox) c)
                .findFirst()
                .orElseThrow();
    }

    private Button findButton(ConditionRow row, String text) {
        return controls(row).getChildren()
                .filter(c -> c instanceof Button)
                .map(c -> (Button) c)
                .filter(b -> text.equals(b.getText()))
                .findFirst()
                .orElseThrow();
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
