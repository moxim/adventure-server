package com.pdg.adventure.api;

import java.util.List;

public interface Actionable extends Describable {

    List<Command> getCommands();

    void addCommand(Command aCommand);

    void removeCommand(Command aCommand);

    ExecutionResult applyCommand(CommandDescription aCommandDescription);

    boolean hasVerb(String aVerb);

    List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription);

    CommandProvider getCommandProvider();
}
