package com.pdg.adventure.api;

import java.util.List;

public interface CommandProvider extends Ided {
    void addCommand(Command aCommand);
    void removeCommand(Command aCommand);
    List<Command> getCommands();
    List<CommandChain> getMatchingCommands(CommandDescription aCommandDescription);
}
