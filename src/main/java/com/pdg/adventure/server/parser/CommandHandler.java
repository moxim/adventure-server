package com.pdg.adventure.server.parser;

import java.util.List;

import com.pdg.adventure.api.*;

/**
 * Reusable command-handling component. Wraps a {@link GenericCommandProvider} and exposes
 * the full {@link HasCommands} contract. Both {@code Thing} and {@code GenericDirection}
 * compose an instance of this class rather than duplicating the delegation boilerplate.
 */
public class CommandHandler implements HasCommands {

    private GenericCommandProvider commandProvider;

    public CommandHandler() {
        commandProvider = new GenericCommandProvider();
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
        return commandProvider.getMatchingCommandChain(aCommandDescription);
    }

    @Override
    public ExecutionResult applyCommand(CommandDescription aCommandDescription) {
        return commandProvider.applyCommand(aCommandDescription);
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
