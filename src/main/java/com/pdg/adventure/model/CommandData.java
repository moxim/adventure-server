package com.pdg.adventure.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.basics.BasicData;
import com.pdg.adventure.model.basics.CommandDescriptionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommandData extends BasicData {
    private CommandDescriptionData commandDescription = new CommandDescriptionData();
    private List<PreCondition> preConditions = new ArrayList<>();
    private List<Action> followUpActions = new ArrayList<>();
    private Action action;
}
