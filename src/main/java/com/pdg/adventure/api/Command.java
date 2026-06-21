package com.pdg.adventure.api;

import java.util.List;

public interface Command extends Ided {

    CommandDescription getDescription();

    ExecutionResult execute();

    void addPreCondition(PreCondition aCondition);

    void addAction(Action anAction);

    List<PreCondition> getPreconditions();

    List<Action> getActions();
}
