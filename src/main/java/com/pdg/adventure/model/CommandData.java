package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basic.BasicData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.condition.PreConditionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommandData extends BasicData {
    private CommandDescriptionData commandDescription;
    private List<PreConditionData> preConditions;
    private List<ActionData> actions;

    public CommandData() {
        this(new CommandDescriptionData());
    }

    public CommandData(CommandDescriptionData aCommandDescriptionData) {
        commandDescription = aCommandDescriptionData;
        preConditions = new ArrayList<>();
        actions = new ArrayList<>();
    }

    /** Append an action. Null-checks, preserving the old setAction guard intent. */
    public void addAction(ActionData anAction) {
        if (anAction == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        actions.add(anAction);
    }
}
