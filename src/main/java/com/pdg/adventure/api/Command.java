package com.pdg.adventure.api;

import java.util.List;

public interface Command extends Ided {

    CommandDescription getDescription();

    ExecutionResult execute();

    void addPreCondition(PreCondition aCondition);

    void addFollowUpAction(Action anAction);

    Action getAction();

    List<PreCondition> getPreconditions();

    List<Action> getFollowUpActions();
}
