package com.pdg.adventure.server.api;

import com.pdg.adventure.server.parser.CommandDescription;

public interface Command {

    CommandDescription getDescription();

    boolean execute();

    void addPreCondition(PreCondition aCondition);

    void addFollowUpAction(Action anAction);
}
