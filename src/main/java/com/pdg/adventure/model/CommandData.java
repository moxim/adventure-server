package com.pdg.adventure.model;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.basics.BasicData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommandData//<SomeAction>
 extends BasicData {
    private CommandDescriptionData commandDescription = new CommandDescriptionData();
    private List<//? super
            PreCondition> preConditions = new ArrayList<>();
    private List<//Some
            Action>
            followUpActions = new ArrayList<>();
    private //Some
    Action action;
}

//public class SomeAction<T extends ActionData> {
//}
