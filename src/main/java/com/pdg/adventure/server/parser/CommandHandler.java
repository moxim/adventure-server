package com.pdg.adventure.server.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.pdg.adventure.api.*;

/**
 * Reusable command-handling component. Wraps a {@link GenericCommandProvider} and exposes
 * the full {@link HasCommands} contract. Both {@code Thing} and {@code GenericDirection}
 * compose an instance of this class rather than duplicating the delegation boilerplate.
 */
public class CommandHandler implements HasCommands {

    private GenericCommandProvider commandProvider;
    private String examineFallbackVerb;
    private Supplier<String> examineFallbackDescription;

    public CommandHandler() {
        commandProvider = new GenericCommandProvider();
    }

    public void setExamineFallback(String aVerb, Supplier<String> aDescription) {
        examineFallbackVerb = aVerb;
        examineFallbackDescription = aDescription;
    }

    @Override
    public void addCommand(Command aCommand) {
        commandProvider.addCommand(aCommand);
    }

    @Override
    public void removeCommand(Command aCommand) {
        commandProvider.removeCommand(aCommand);
    }

    @Override
    public boolean hasVerb(String aVerb) {
        return commandProvider.hasVerb(aVerb);
    }

    @Override
    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        List<CommandChain> result = commandProvider.getMatchingCommandChain(aCommandDescription);
        if (result.isEmpty()
                && examineFallbackVerb != null
                && examineFallbackVerb.equals(aCommandDescription.getVerb())
                && examineFallbackDescription != null) {
            GenericCommandChain chain = new GenericCommandChain();
            Supplier<String> description = examineFallbackDescription;
            chain.addCommand(new GenericCommand(
                    new GenericCommandDescription(examineFallbackVerb),
                    new ExamineFallbackAction(description)));
            result = new ArrayList<>(List.of(chain));
        }
        return result;
    }

    @Override
    public ExecutionResult applyCommand(CommandDescription aCommandDescription) {
        ExecutionResult result = new CommandExecutionResult();
        for (CommandChain chain : getMatchingCommandChain(aCommandDescription)) {
            result = chain.execute();
            result.setCommandHasMatched();
        }
        return result;
    }

    @Override
    public List<Command> getCommands() {
        return commandProvider.getCommands();
    }

    @Override
    public GenericCommandProvider getCommandProvider() {
        return commandProvider;
    }

    public void setCommandProvider(GenericCommandProvider aCommandProvider) {
        commandProvider = aCommandProvider;
    }
}
