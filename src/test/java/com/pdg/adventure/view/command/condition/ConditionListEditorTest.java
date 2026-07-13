package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

class ConditionListEditorTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        adventureData.setLocationData(new HashMap<>());
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void constructor_buildsUI() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void setConditions_withNullList_doesNotThrow() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(null);
        assertThat(editor.getConditions()).isEmpty();
    }

    @Test
    void setConditions_withEmptyList_producesNoRows() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(new ArrayList<>());
        assertThat(editor.getConditions()).isEmpty();
    }

    @Test
    void setConditions_withLeafCondition_producesOneRow() {
        CarriedConditionData carried = new CarriedConditionData();
        carried.setItemId("item-1");

        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(carried));

        List<PreConditionData> result = editor.getConditions();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(CarriedConditionData.class);
    }

    @Test
    void setConditions_withNotConditionData_producesNegatedRow() {
        CarriedConditionData leaf = new CarriedConditionData();
        leaf.setItemId("item-1");
        NotConditionData not = new NotConditionData();
        not.setPreCondition(leaf);

        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(not));

        List<PreConditionData> result = editor.getConditions();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(NotConditionData.class);
        assertThat(((NotConditionData) result.getFirst()).getPreCondition()).isInstanceOf(CarriedConditionData.class);
    }

    @Test
    void getConditions_afterSetConditions_roundTripsLeafData() {
        CarriedConditionData carried = new CarriedConditionData();
        carried.setId("round-trip-id");
        carried.setItemId("sword");

        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(carried));

        List<PreConditionData> result = editor.getConditions();
        assertThat(result).hasSize(1);
        CarriedConditionData retrieved = (CarriedConditionData) result.getFirst();
        assertThat(retrieved.getId()).isEqualTo("round-trip-id");
        assertThat(retrieved.getItemId()).isEqualTo("sword");
    }

    @Test
    void moveDown_onFirstRow_reordersConditions() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(carried("first"), carried("second")));

        clickButton(rowsOf(editor).getFirst(), "Down");

        List<PreConditionData> result = editor.getConditions();
        assertThat(((CarriedConditionData) result.get(0)).getItemId()).isEqualTo("second");
        assertThat(((CarriedConditionData) result.get(1)).getItemId()).isEqualTo("first");
    }

    @Test
    void moveUp_onSecondRow_reordersConditions() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(carried("first"), carried("second")));

        clickButton(rowsOf(editor).get(1), "Up");

        List<PreConditionData> result = editor.getConditions();
        assertThat(((CarriedConditionData) result.get(0)).getItemId()).isEqualTo("second");
        assertThat(((CarriedConditionData) result.get(1)).getItemId()).isEqualTo("first");
    }

    @Test
    void moveUp_onFirstRow_isNoOp() {
        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(carried("first"), carried("second")));

        clickButton(rowsOf(editor).getFirst(), "Up");

        List<PreConditionData> result = editor.getConditions();
        assertThat(((CarriedConditionData) result.get(0)).getItemId()).isEqualTo("first");
        assertThat(((CarriedConditionData) result.get(1)).getItemId()).isEqualTo("second");
    }

    private CarriedConditionData carried(String itemId) {
        CarriedConditionData data = new CarriedConditionData();
        data.setItemId(itemId);
        return data;
    }

    private List<ConditionRow> rowsOf(ConditionListEditor editor) {
        return editor.getChildren()
                .filter(c -> c instanceof VerticalLayout)
                .findFirst()
                .map(rowsLayout -> rowsLayout.getChildren()
                        .filter(c -> c instanceof ConditionRow)
                        .map(c -> (ConditionRow) c)
                        .toList())
                .orElseThrow();
    }

    // ConditionRow (Details) -> Div -> HorizontalLayout(controls) -> Button
    private void clickButton(ConditionRow row, String text) {
        row.getChildren()
                .filter(c -> c instanceof Div)
                .findFirst()
                .flatMap(div -> ((Div) div).getChildren()
                        .filter(c -> c instanceof HorizontalLayout)
                        .findFirst())
                .flatMap(hl -> ((HorizontalLayout) hl).getChildren()
                        .filter(c -> c instanceof Button)
                        .map(c -> (Button) c)
                        .filter(b -> text.equals(b.getText()))
                        .findFirst())
                .orElseThrow()
                .click();
    }

    @Test
    void editingLeafField_updatesRowSummaryLive() {
        ItemData sword = new ItemData();
        sword.setId("sword-1");
        DescriptionData desc = new DescriptionData();
        desc.setShortDescription("Sword");
        sword.setDescriptionData(desc);
        List<ItemData> pocketItems = new ArrayList<>();
        pocketItems.add(sword);
        adventureData.getPlayerPocket().setItems(pocketItems);

        ConditionListEditor editor = new ConditionListEditor(adventureData);
        editor.setConditions(List.of(new CarriedConditionData()));
        ConditionRow row = rowsOf(editor).getFirst();
        assertThat(row.getSummaryText()).isEqualTo("Carried: (none)");

        // Simulate the user picking an item in the editor's combo box — no manual refresh.
        setFirstEditorField(row, sword);

        assertThat(row.getSummaryText()).isEqualTo("Carried: Sword");
    }

    // ConditionRow (Details) -> Div -> ConditionEditorComponent -> first HasValue field
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setFirstEditorField(ConditionRow row, Object value) {
        ConditionEditorComponent editor = row.getChildren()
                .filter(c -> c instanceof Div).findFirst()
                .flatMap(div -> ((Div) div).getChildren()
                        .filter(c -> c instanceof ConditionEditorComponent).findFirst())
                .map(c -> (ConditionEditorComponent) c)
                .orElseThrow();
        HasValue field = (HasValue) editor.getChildren()
                .filter(c -> c instanceof HasValue)
                .findFirst()
                .orElseThrow();
        field.setValue(value);
    }
}
