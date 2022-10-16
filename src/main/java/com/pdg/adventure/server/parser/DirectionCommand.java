package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class DirectionCommand extends GenericCommand {
    private final Location destination;
    private final String verb;

    public DirectionCommand(CommandDescription aCommandDescription, MovePlayerAction anAction, Vocabulary aVocabulary) {
        super(aCommandDescription.getVerb(), anAction, aVocabulary);
        destination = anAction.getDestination();
        verb = aCommandDescription.getVerb();
    }

    public Location getDestination() {
        return destination;
    }

    public String getVerb() {
        return verb;
    }
}
