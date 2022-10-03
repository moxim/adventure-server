package com.pdg.adventure.server.api;

import java.util.List;

public interface Actionable {
    List<Action> getActions();
    void addAction(Action anAction);
    void removeAction(Action anAction);

//    void executeAction(Action anAction, Actor anActor);
//    void executeCommandFor(Actor anActor, Command aCommand);
}
