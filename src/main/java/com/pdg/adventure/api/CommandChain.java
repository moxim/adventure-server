package com.pdg.adventure.api;

import java.util.List;

public interface CommandChain extends Ided {

    void addCommand(Command aCommand);
    void removeCommand(Command aCommand);

    List<Command> getCommands();

    ExecutionResult execute();
}
