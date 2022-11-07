package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.Action;
import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.PreCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericCommand implements Command {
    private final CommandDescription commandDescription;
    private final List<PreCondition> preConditions;
    private final List<Action> followUpActions;
    private final Action action;
    private final UUID id;

    public GenericCommand(CommandDescription aCommandDescription, Action anAction) {
        commandDescription = aCommandDescription;
        preConditions = new ArrayList<>();
        followUpActions = new ArrayList<>();
        action = anAction;
        id = UUID.randomUUID();
    }

    @Override
    public CommandDescription getDescription() {
        return commandDescription;
    }

    @Override
    public boolean execute() {
        if (canFulfillPreconditions()) {
            executeAction();
            executeFollowupActions();
            return true;
        }
        return false;
    }

    public void executeAction() {
        action.execute();
    }

    private void executeFollowupActions() {
        for (Action followUpAction : followUpActions) {
            followUpAction.execute();
        }
    }

    private boolean canFulfillPreconditions() {
        for (PreCondition condition : preConditions) {
            if (!condition.isValid()) {
                return false;
            }
        }
        return true;
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

    public String toString() {
        return commandDescription.getDescription() + (action == null ? "" : "[" + action + "]");
    }
}
