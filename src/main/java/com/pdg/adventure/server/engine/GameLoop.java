package com.pdg.adventure.server.engine;

import java.io.BufferedReader;
import java.io.IOException;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.QuitException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.vocabulary.Vocabulary;

public class GameLoop {
    private final Parser parser;

    public GameLoop(Parser aParser) {
        parser = aParser;
    }

    public void run(BufferedReader aReader) {
        boolean endLoop = true;
        while (endLoop) {
            Location currentLocation = Environment.getCurrentLocation();
            try {
                Environment.preProcessCommands();

                GenericCommandDescription command = parser.getInput(aReader);
                // Check commands that are independent of locations like inventory, save, quit aso.
                ExecutionResult result = Environment.interceptCommands(command);
                if (result.getExecutionState() == ExecutionResult.State.FAILURE) {
                    result = currentLocation.applyCommand(command);
                }
                if (!Vocabulary.EMPTY_STRING.equals(result.getResultMessage())) {
                    Environment.tell(result.getResultMessage());
                }
            } catch (QuitException anException) {
                Environment.tell(anException.getMessage());
                endLoop = false;
            } catch (IOException | RuntimeException anException) {
                anException.printStackTrace();
                endLoop = false;
            }
        }
    }
}
