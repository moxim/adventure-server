package com.pdg.adventure.server.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.exception.QuitException;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
import com.pdg.adventure.server.parser.CommandExecutor;
import com.pdg.adventure.server.parser.CommandSequence;
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
        try {
            CommandSequence sequence = parser.handle(anInput);
            for (GenericCommandDescription command : sequence.commands()) {
                if (!runOneCommandSucceeded(command)) {
                    break; // stop the sequence at the first sub-command that failed
                }
            }
            return CommandOutcome.CONTINUE;
        } catch (QuitException anException) {
            gameContext.tell(anException.getMessage());
            return CommandOutcome.QUIT;
        } catch (UnresolvedReferenceException anException) {
            gameContext.tell(anException.getMessage());
            return CommandOutcome.CONTINUE;
        } catch (ReloadAdventureException e) {
            throw e;
        } catch (RuntimeException anException) {
            LOG.error("An error occurred during the game loop.", anException);
            return CommandOutcome.ERROR;
        }
    }

    /**
     * Runs one already-parsed sub-command of a (possibly conjunction-joined) turn: checks
     * interceptor and location/pocket commands, and tells the result. Re-reads
     * gameContext.getCurrentLocation()/getPocket() rather than reusing a value captured once
     * for the whole turn, because an earlier sub-command in the same sequence (e.g. "go north")
     * may have moved the player, and this sub-command must see that new location.
     *
     * @return true if this sub-command succeeded and the sequence should continue; false if it
     * failed (or couldn't be understood) and the remaining sub-commands must not be attempted.
     */
    private boolean runOneCommandSucceeded(GenericCommandDescription command) {
        // Check commands that are independent of locations, like inventory, save, quit aso.
        ExecutionResult result = gameContext.interceptCommands(command);
        if (result.getExecutionState() != ExecutionResult.State.FAILURE) {
            gameContext.tell(result.getResultMessage());
            return true;
        }

        // Continue if the user provided nothing that we understand. A command needs at
        // least a verb to be actionable - e.g. a bare noun like "suit" with no verb - so
        // that alone is unparseable, not merely a command with no matching handler.
        if (VocabularyData.EMPTY_STRING.equals(command.getVerb())) {
            gameContext.tell("I don't understand, please rephrase.");
            // TODO: Review needed — an unparseable sub-command isn't literally a "failure" of a
            //  real command, but the user's "stop at first failure" requirement didn't cover this
            //  case explicitly. Treating it as a stop condition here, consistent with the rest of
            //  the sequence's fail-fast behaviour.
            return false;
        }

        // Check commands that are possible because of the players inventory or the current location.
        CommandExecutor commandExecuter = new CommandExecutor(gameContext.getPocket(), gameContext.getCurrentLocation());
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
        return result.getExecutionState() != ExecutionResult.State.FAILURE;
    }
}
