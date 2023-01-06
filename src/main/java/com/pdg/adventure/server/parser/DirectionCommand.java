package com.pdg.adventure.server.parser;

import com.pdg.adventure.api.Action;

public class DirectionCommand extends GenericCommand {

    public DirectionCommand(GenericCommandDescription aCommandDescription, Action anAction) {
        super(aCommandDescription, anAction);
    }
}
