package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
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
}
