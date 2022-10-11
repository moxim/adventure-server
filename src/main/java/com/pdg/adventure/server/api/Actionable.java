package com.pdg.adventure.server.api;

import java.util.List;

public interface Actionable {

    List<Command> getCommands();

    void addCommand(Command aCommand);

    void removeCommand(Command aCommand);

    boolean applyCommand(String aVerb);
}
