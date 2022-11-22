package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.Action;
import com.pdg.adventure.server.api.Command;
import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.api.PreCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericCommand implements Command {
    private final GenericCommandDescription commandDescription;
    private final List<PreCondition> preConditions;
    private final List<Action> followUpActions;
    private final Action action;
    private final UUID id;

    public GenericCommand(GenericCommandDescription aCommandDescription, Action anAction) {
        commandDescription = aCommandDescription;
        preConditions = new ArrayList<>();
        followUpActions = new ArrayList<>();
        action = anAction;
        id = UUID.randomUUID();
    }

    @Override
    public GenericCommandDescription getDescription() {
        return commandDescription;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = checkPreconditions();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            ExecutionResult fromAction = action.execute();
            setExecutionResult(result, fromAction);
            if (fromAction.getExecutionState() == ExecutionResult.State.SUCCESS) {
                ExecutionResult fromFollowupActions = executeFollowupActions();
                if (fromFollowupActions.getExecutionState() == ExecutionResult.State.FAILURE) {
                    setExecutionResult(result, fromFollowupActions);
                }
            }
        }
        return result;
    }

    private ExecutionResult executeFollowupActions() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        for (Action followUpAction : followUpActions) {
            ExecutionResult tmp = followUpAction.execute();
            if (tmp.getExecutionState() == ExecutionResult.State.FAILURE) {
                setExecutionResult(result, tmp);
                break;
            }
        }
        return result;
    }

    private ExecutionResult checkPreconditions() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        for (PreCondition condition : preConditions) {
            ExecutionResult tmp = condition.check();
            if (tmp.getExecutionState() == ExecutionResult.State.FAILURE) {
                setExecutionResult(result, tmp);
                break;
            }
        }
        return result;
    }

    private void setExecutionResult(ExecutionResult aTarget, ExecutionResult aResult) {
        aTarget.setExecutionState(aResult.getExecutionState());
        aTarget.setResultMessage(aResult.getResultMessage());
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
