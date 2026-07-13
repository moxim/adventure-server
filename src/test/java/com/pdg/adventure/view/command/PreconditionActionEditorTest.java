package com.pdg.adventure.view.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.PreConditionData;

class PreconditionActionEditorTest {

    @Test
    void saveToCommand_persistsConditionsFromEditor() {
        PreconditionActionEditor editor = new PreconditionActionEditor(new AdventureData());

        CarriedConditionData carried = new CarriedConditionData();
        carried.setItemId("sword");
        CommandData source = new CommandData();
        source.setPreConditions(List.of(carried));
        editor.setCommand(source);

        CommandData target = new CommandData();
        editor.saveToCommand(target);

        List<PreConditionData> saved = target.getPreConditions();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst()).isInstanceOf(CarriedConditionData.class);
    }

    @Test
    void saveToCommand_writesActions_emptyRoundTripStaysEmpty() {
        PreconditionActionEditor editor = new PreconditionActionEditor(new AdventureData());

        CommandData source = new CommandData();
        editor.setCommand(source);

        CommandData target = new CommandData();
        editor.saveToCommand(target);

        List<ActionData> savedActions = target.getActions();
        assertThat(savedActions).isNotNull();
        assertThat(savedActions).isEmpty();
    }
}
