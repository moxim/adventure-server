package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.support.Environment;

import java.io.BufferedReader;
import java.io.IOException;

public class GameLoop {
    private final Parser parser;

    public GameLoop(Parser aParser) {
        parser = aParser;
    }

    public void run(BufferedReader aReader) {
        while (true) {
            Location currentLocation = Environment.getCurrentLocation();
            try {
                CommandDescription command = parser.getInput(aReader);
                String verb = command.getVerb();
                if ("quit".equals(verb)) {
                    break;
                }
                if (!currentLocation.applyCommand(command)) {
                    Environment.tell("You can't do that.");
                }
            } catch (IOException aE) {
                Environment.tell(aE.getMessage());
                break;
            }
        }
    }
}
