package com.pdg.adventure.server.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.exception.QuitException;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandExecutor;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;

public class GameLoop {
    private static final Logger LOG = LoggerFactory.getLogger(GameLoop.class);

    public enum CommandOutcome { CONTINUE, QUIT, ERROR }

    private final Parser parser;
    private final GameContext gameContext;

    public GameLoop(Parser aParser, GameContext aGameContext) {
        parser = aParser;
        gameContext = aGameContext;
    }

    public void run(BufferedReader aReader) {
        boolean keepLooping = true;
        while (keepLooping) {
            try {
                // Run workflow actions.
                gameContext.preProcessCommands();

                // Obtain user input.
                String input = aReader.readLine();

                keepLooping = processCommand(input) == CommandOutcome.CONTINUE;
            } catch (IOException anException) {
                LOG.error("An error occurred during the game loop.", anException);
                keepLooping = false;
            }
        }
    }

    /**
     * Runs one already-obtained line of input through the engine: parses it, checks interceptor
     * and location/pocket commands, and tells the result. Callers that need the author's workflow
     * pre-commands to fire every turn (e.g. run(BufferedReader)'s console prompt) must call
     * gameContext.preProcessCommands() themselves before this — it is not called here, so a
     * caller driving one command per external event (e.g. a browser input) controls that timing.
     */
    public CommandOutcome processCommand(String anInput) {
        Location currentLocation = gameContext.getCurrentLocation();
        try {
            GenericCommandDescription command = parser.handle(anInput);

            // Check commands that are independent of locations, like inventory, save, quit aso.
            ExecutionResult result = gameContext.interceptCommands(command);
            if (result.getExecutionState() != ExecutionResult.State.FAILURE) {
                gameContext.tell(result.getResultMessage());
                return CommandOutcome.CONTINUE;
            }

            // Continue if the user provided nothing that we understand.
            if (command.toString().equals("||")) {
                gameContext.tell("I don't understand, please rephrase.");
                return CommandOutcome.CONTINUE;
            }

            // Check commands that are possible because of the players inventory or the current location.
            CommandExecutor commandExecuter = new CommandExecutor(gameContext.getPocket(), currentLocation);
            result = commandExecuter.execute(command);

            if (!VocabularyData.EMPTY_STRING.equals(result.getResultMessage())) {
                gameContext.tell(result.getResultMessage());
            } else {
                if (result.getExecutionState() == ExecutionResult.State.FAILURE) {
                    gameContext.tell("I can't do that.");
                } else {
                    gameContext.tell("Done.");
                }
            }
            return CommandOutcome.CONTINUE;
        } catch (QuitException anException) {
            gameContext.tell(anException.getMessage());
            return CommandOutcome.QUIT;
        } catch (ReloadAdventureException e) {
            throw e;
        } catch (RuntimeException anException) {
            LOG.error("An error occurred during the game loop.", anException);
            return CommandOutcome.ERROR;
        }
    }
}
