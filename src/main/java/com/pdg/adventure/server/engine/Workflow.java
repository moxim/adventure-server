package com.pdg.adventure.server.engine;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandDescription;

public class Workflow {

    // Iteration order for preCommands each turn: alphabetical by verb, then adjective, then noun -
    // independent of the TreeMap's own key ordering (which sorts by the "verb|adjective|noun"
    // description string and, because '|' sorts after letters, would rank e.g. "go" after "goto").
    private static final Comparator<CommandDescription> ALPHABETICAL = Comparator
            .comparing(CommandDescription::getVerb, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(CommandDescription::getAdjective, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(CommandDescription::getNoun, String.CASE_INSENSITIVE_ORDER);

    private final Map<CommandDescription, Command> preCommands;
    private final Map<CommandDescription, Command> interceptorCommands;
    private final GameContext gameContext;

    public Workflow(GameContext aGameContext) {
        preCommands = new TreeMap<>();
        interceptorCommands = new TreeMap<>();
        gameContext = aGameContext;
    }

    public void addPreCommand(GenericCommandDescription aCommandDescription, Command aCommand) {
        preCommands.put(aCommandDescription, aCommand);
    }

    public void addInterceptorCommand(GenericCommandDescription aCommandDescription, Command aCommand) {
        interceptorCommands.put(aCommandDescription, aCommand);
    }

    public void removePreCommand(GenericCommandDescription aCommandDescription, Command aCommand) {
        preCommands.remove(aCommandDescription, aCommand);
    }

    public void removeInterceptorCommand(GenericCommandDescription aCommandDescription, Command aCommand) {
        interceptorCommands.remove(aCommandDescription, aCommand);
    }

    public void preProcess() {
        process(preCommands);
    }

    public ExecutionResult interceptCommands(CommandDescription aCommand) {
        ExecutionResult result = new CommandExecutionResult();
        Command command = interceptorCommands.get(aCommand);
        if (command != null) {
            result = command.execute();
        }
        return result;
    }

    private void process(Map<CommandDescription, Command> commands) {
        commands.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(ALPHABETICAL))
                .forEach(commandEntry -> {
                    ExecutionResult result = commandEntry.getValue().execute();
                    gameContext.tell(result.getResultMessage());
                });
    }
}
