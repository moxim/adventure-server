package com.pdg.adventure.server.parser;

import com.pdg.adventure.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericCommand implements Command {
    private final CommandDescription commandDescription;
    private final List<PreCondition> preConditions;
    private final List<Action> followUpActions;
    private final Action mainAction;
    private String id;

    public GenericCommand(CommandDescription aCommandDescription, Action anAction) {
        commandDescription = aCommandDescription;
        preConditions = new ArrayList<>();
        followUpActions = new ArrayList<>();
        mainAction = anAction;
        id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
    }

    public Action getAction() {return mainAction;}

    @Override
    public List<PreCondition> getPreconditions() {
        return preConditions;
    }

    @Override
    public List<Action> getFollowUpActions() {
    return followUpActions;
    }

    @Override
    public CommandDescription getDescription() {
        return commandDescription;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = checkPreconditions();
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            ExecutionResult fromAction = mainAction.execute();
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

    /**
     * Add another action that should happen if the mainAction can happen.
     * (e.g. if the player opens a door he forgot to check for traps a trap sets off)
     * @param anAction
     */
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
        return commandDescription.getDescription() + (mainAction == null ? "" : "[" + mainAction + "]");
    }
}
