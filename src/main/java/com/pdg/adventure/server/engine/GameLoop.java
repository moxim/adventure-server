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
                Environment.preProcessCommands();

                CommandDescription command = parser.getInput(aReader);

                // Check commands that are independent of locations like inventory, save, quit aso.
                // Some of those may have been not available at a specific location, so check them after checking
                // them on locations.
                if (!Environment.interceptCommands(command) && !currentLocation.applyCommand(command)) {
                    Environment.tell("You can't do that.");
                }
            } catch (IOException | RuntimeException anException) {
                Environment.tell(anException.getMessage());
                break;
            }
        }
    }
}
