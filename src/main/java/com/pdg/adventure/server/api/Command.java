package com.pdg.adventure.server.api;

public interface Command {

    CommandDescription getDescription();

    ExecutionResult execute();

    void addPreCondition(PreCondition aCondition);

    void addFollowUpAction(Action anAction);
}
