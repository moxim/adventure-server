package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basics.BasicData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.model.condition.PreConditionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommandData
 extends BasicData {
    private CommandDescriptionData commandDescription = new CommandDescriptionData();
    private List<? extends PreConditionData> preConditions = new ArrayList<>();
    private List<? extends ActionData>
            followUpActions = new ArrayList<>();
    private ActionData action;
}

