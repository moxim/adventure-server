package com.pdg.adventure.api;

import java.util.List;

/**
 * Separable command-handling capability. Extracted from {@link Actionable} so that any class
 * can compose command behaviour without also being required to be {@link Describable}.
 * <p>
 * {@link Actionable} extends both {@link Describable} and this interface; non-Describable
 * objects (e.g. a standalone {@code CommandHandler} component) implement only this interface.
 */
public interface HasCommands {

    List<Command> getCommands();

    void addCommand(Command aCommand);

    void removeCommand(Command aCommand);

    ExecutionResult applyCommand(CommandDescription aCommandDescription);

    boolean hasVerb(String aVerb);

    List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription);

    CommandProvider getCommandProvider();
}
