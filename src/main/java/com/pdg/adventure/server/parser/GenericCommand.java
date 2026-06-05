package com.pdg.adventure.server.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.pdg.adventure.api.*;

public class GenericCommand implements Command {
    private final CommandDescription commandDescription;
    private final List<PreCondition> preConditions;
    private final List<Action> actions;
    private String id;

    public GenericCommand(CommandDescription aCommandDescription) {
        commandDescription = aCommandDescription;
        preConditions = new ArrayList<>();
        actions = new ArrayList<>();
        id = UUID.randomUUID().toString();
    }

    /** Convenience: seed the command with its first action (sugar, not a privileged action). */
    public GenericCommand(CommandDescription aCommandDescription, Action anAction) {
        this(aCommandDescription);
        if (anAction != null) {
            actions.add(anAction);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
    }

    @Override
    public List<PreCondition> getPreconditions() {
        return preConditions;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public CommandDescription getDescription() {
        return commandDescription;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = checkPreconditions();
        if (result.getExecutionState() != ExecutionResult.State.SUCCESS) {
            return result;                 // preconditions unmet → command does not apply
        }
        List<String> messages = new ArrayList<>();
        for (Action action : actions) {
            ExecutionResult fromAction = action.execute();
            if (fromAction.getExecutionState() == ExecutionResult.State.FAILURE) {
                result.setExecutionState(ExecutionResult.State.FAILURE);
                result.setResultMessage(fromAction.getResultMessage());
                return result;             // a failing action fails the whole command
            }
            String msg = fromAction.getResultMessage();
            if (msg != null && !msg.isBlank()) {
                messages.add(msg);
            }
        }
        result.setResultMessage(String.join(System.lineSeparator(), messages));
        return result;                     // SUCCESS, all action messages joined
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
    public void addAction(Action anAction) {
        actions.add(anAction);
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
        return commandDescription.getDescription() + (actions.isEmpty() ? "" : actions.toString());
    }
}
