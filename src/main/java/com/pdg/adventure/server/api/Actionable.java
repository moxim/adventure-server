package com.pdg.adventure.server.api;

import java.util.List;

public interface Actionable {

    List<Command> getCommands();

    void addCommand(Command aCommand);

    void removeCommand(Command aCommand);

//    void executeAction(Action anAction, Actor anActor);
//    void executeCommandFor(Actor anActor, GenericCommand aCommand);
}
