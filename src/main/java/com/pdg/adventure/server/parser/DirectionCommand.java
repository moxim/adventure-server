package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class DirectionCommand extends GenericCommand {
    private final Location destination;

    public DirectionCommand(CommandDescription aCommandDescription, MovePlayerAction anAction, Vocabulary aVocabulary) {
        super(aCommandDescription.getVerb(), anAction, aVocabulary);
        destination = anAction.getDestination();
    }

    public Location getDestination() {
        return destination;
    }
}
