package com.pdg.adventure.view.command.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.condition.*;

class ConditionEditorFactoryTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        adventureData.setLocationData(new HashMap<>());
        adventureData.setPlayerPocket(new ItemContainerData("pocket"));
    }

    @Test
    void createEditor_withCarriedConditionData_returnsCarriedEditor() {
        ConditionEditorComponent editor = ConditionEditorFactory.createEditor(new CarriedConditionData(), adventureData);
        assertThat(editor).isInstanceOf(CarriedConditionEditor.class);
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void createEditor_withHereConditionData_returnsHereEditor() {
        assertThat(ConditionEditorFactory.createEditor(new HereConditionData(), adventureData))
                .isInstanceOf(HereConditionEditor.class);
    }

    @Test
    void createEditor_withWornConditionData_returnsWornEditor() {
        assertThat(ConditionEditorFactory.createEditor(new WornConditionData(), adventureData))
                .isInstanceOf(WornConditionEditor.class);
    }

    @Test
    void createEditor_withPlayerAtConditionData_returnsPlayerAtEditor() {
        assertThat(ConditionEditorFactory.createEditor(new PlayerAtConditionData(), adventureData))
                .isInstanceOf(PlayerAtConditionEditor.class);
    }

    @Test
    void createEditor_withItemAtConditionData_returnsItemAtEditor() {
        assertThat(ConditionEditorFactory.createEditor(new ItemAtConditionData(), adventureData))
                .isInstanceOf(ItemAtConditionEditor.class);
    }

    @Test
    void createEditor_withEqualsConditionData_returnsEqualsEditor() {
        assertThat(ConditionEditorFactory.createEditor(new EqualsConditionData(), adventureData))
                .isInstanceOf(EqualsConditionEditor.class);
    }

    @Test
    void createEditor_withGreaterThanConditionData_returnsGreaterThanEditor() {
        assertThat(ConditionEditorFactory.createEditor(new GreaterThanConditionData(), adventureData))
                .isInstanceOf(GreaterThanConditionEditor.class);
    }

    @Test
    void createEditor_withLowerThanConditionData_returnsLowerThanEditor() {
        assertThat(ConditionEditorFactory.createEditor(new LowerThanConditionData(), adventureData))
                .isInstanceOf(LowerThanConditionEditor.class);
    }

    @Test
    void createEditor_withSameConditionData_returnsSameEditor() {
        assertThat(ConditionEditorFactory.createEditor(new SameConditionData(), adventureData))
                .isInstanceOf(SameConditionEditor.class);
    }

    @Test
    void createEditor_withUnknownType_throwsUnsupportedOperationException() {
        PreConditionData unknown = new PreConditionData() {};
        assertThatThrownBy(() -> ConditionEditorFactory.createEditor(unknown, adventureData))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("No editor available for condition type");
    }

    @Test
    void createEditor_returnsInitializedEditor() {
        ConditionEditorComponent editor = ConditionEditorFactory.createEditor(new CarriedConditionData(), adventureData);
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }
}
