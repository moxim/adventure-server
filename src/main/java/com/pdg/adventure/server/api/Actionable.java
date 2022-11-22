package com.pdg.adventure.server.api;

import java.util.List;

public interface Actionable {

    List<Command> getCommands();

    void addCommand(Command aCommand);

    void removeCommand(Command aCommand);

    ExecutionResult applyCommand(CommandDescription aVerb);

    boolean hasVerb(String aVerb);

    List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription);
}
