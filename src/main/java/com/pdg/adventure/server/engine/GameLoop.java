package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class GameLoop {
    private final Vocabulary vocabulary;
    private final Parser parser;

    private Location currentLocation;

    public GameLoop(Vocabulary aVocabulary, Location aLocation) {
        vocabulary = aVocabulary;
        currentLocation = aLocation;
        parser = new Parser(vocabulary);
    }

    public void run() {
        //  game loop
        while (true) {
            CommandDescription command = parser.getInput();
            String verb = command.getVerb();
            if ("quit".equals(verb)) {
                break;
            }
            currentLocation.applyCommand(command);
        }
    }
}
