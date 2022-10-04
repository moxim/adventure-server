package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.Action;
import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.PreCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericCommand implements Command {
    private final String verb;
    private final List<PreCondition> preConditions;
    private final List<Action> followUpActions;
    private final Action action;
    private final UUID id;

    public GenericCommand(String aVerb, Action anAction) {
        verb = aVerb;
        action = anAction;
        id = UUID.randomUUID();
        preConditions = new ArrayList<>();
        followUpActions = new ArrayList<>();
    }

    @Override
    public String getDescription() {
        return verb;
    }

    @Override
    public void execute() {
        boolean canExecute = true;
        for (PreCondition condition : preConditions) {
            if (!condition.isValid()) {
                canExecute = false;
                break;
            }
        }
        if (canExecute) {
            action.execute();
            for (Action action : followUpActions) {
                action.execute();
            }
        }

    }

    @Override
    public void addPreCondition(PreCondition aCondition) {
        preConditions.add(aCondition);
    }

    @Override
    public void addFollowUpAction(Action anAction) {
        followUpActions.add(anAction);
    }

    @Override
    public boolean equals(Object aO) {
        if (this == aO) return true;
        if (!(aO instanceof GenericCommand aCommand)) return false;
        return id.equals(aCommand.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
