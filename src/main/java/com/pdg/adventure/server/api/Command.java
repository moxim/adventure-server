package com.pdg.adventure.server.api;

public interface Command {

    String getDescription();

    boolean execute();

    void addPreCondition(PreCondition aCondition);

    void addFollowUpAction(Action anAction);
}
