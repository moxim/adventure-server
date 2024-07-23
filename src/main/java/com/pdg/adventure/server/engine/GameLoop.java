package com.pdg.adventure.server.engine;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.action.MessageAction;
import com.pdg.adventure.server.exception.QuitException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecuter;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;

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
                // Run workflow actions.
                Environment.preProcessCommands();

                // Obtain user input.
                GenericCommandDescription command = parser.getInput(aReader);

                // Check commands that are independent of locations, like inventory, save, quit aso.
                ExecutionResult result = Environment.interceptCommands(command);
                if (result.getExecutionState() != ExecutionResult.State.FAILURE) {
                    Environment.tell(result.getResultMessage());
                    continue;
                }

                // Continue if the user provided nothing that we understand.
                if (command.toString().equals("__")) {
                    System.out.println(new MessageAction("I don't understand, please rephrase.", null).execute());
                    continue;
                }

                // Check commands that are possible because of the players inventory or the current location.
                CommandExecuter commandExecuter = new CommandExecuter(Environment.getPocket(), currentLocation);
                result = commandExecuter.execute(command);

                if (!VocabularyData.EMPTY_STRING.equals(result.getResultMessage())) {
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
