package com.pdg.adventure.server.api;

import java.util.List;

public interface Actionable {
    List<Action> getActions();
    void addAction(Action anAction);
    void removeAction(Action anAction);

    List<Command> getCommands();
    void addCommand(Command aDefautCommand);
    void removeCommand(Command aDefautCommand);

//    void executeAction(Action anAction, Actor anActor);
//    void executeCommandFor(Actor anActor, GenericCommand aCommand);
}
