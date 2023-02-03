package com.pdg.adventure.server.action;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.messages.MessagesHolder;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public class IncrementVariableAction extends AbstractVariableAction {
    private final String name;
    private final String value;


    public IncrementVariableAction(String aName, String aValue, VariableProvider aVariableProvider,
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
            // TODO
            //  should this be an exception? it is not part of normal programm execution
            result.setResultMessage("Variable " + name + " does not exist!");
        } else {
            Long envVal = Long.valueOf(envVariable.aValue());
            Long iVal = Long.valueOf(value);
            variableProvider.set(new Variable(name, String.valueOf(Long.valueOf(envVal + iVal))));
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
