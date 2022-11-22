package com.pdg.adventure.server.api;

import java.util.List;

public interface CommandChain {

    void addCommand(Command aCommand);

    List<Command> getCommands();

    ExecutionResult execute();
}
