package com.pdg.adventure.server.action;

import lombok.Getter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@Getter
public class DecrementVariableAction extends AbstractVariableAction {
    private final String name;
    private final Integer value;


    public DecrementVariableAction(String aName, Integer aValue, VariableProvider aVariableProvider,
                                   MessagesHolder aMessagesHolder) {
        super(aVariableProvider, aMessagesHolder);
        name = aName;
        value = aValue;
    }

    @Override
    public ExecutionResult execute() {
        CommandExecutionResult result = new CommandExecutionResult();
        Variable envVariable = variableProvider.get(name);
        if (envVariable == null) {
            // TODO should this be an exception? it is not part of normal programm execution
            result.setResultMessage("Variable " + name + " does not exist!");
        } else {
            Integer envVal = envVariable.value();
            variableProvider.set(new Variable(name, envVal - value));
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
