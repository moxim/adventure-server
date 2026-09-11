package com.pdg.adventure.server.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.exception.QuitException;
import com.pdg.adventure.server.exception.ReloadAdventureException;
import com.pdg.adventure.server.exception.UnresolvedReferenceException;
import com.pdg.adventure.server.parser.CommandExecutor;
import com.pdg.adventure.server.parser.CommandSequence;
import com.pdg.adventure.server.parser.GenericCommandDescription;
import com.pdg.adventure.server.parser.Parser;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

public class GameLoop {
    private static final Logger LOG = LoggerFactory.getLogger(GameLoop.class);

    public enum CommandOutcome { CONTINUE, QUIT, ERROR }

    private final Parser parser;
    private final GameContext gameContext;

    public GameLoop(Parser aParser, GameContext aGameContext) {
        parser = aParser;
        gameContext = aGameContext;
    }

    /**
     * Runs one already-obtained line of input through the engine: parses it into a sequence of
     * sub-commands and, for each in turn, dispatches the sub-command and then runs the author's
     * workflow Processes (gameContext.runProcesses()) against the state that command left behind
     * - stopping at the first sub-command that fails. Processes run after dispatch, not before,
     * so a location-gated Process (e.g. PlayerAtCondition) evaluates the location as it stands
     * after that turn's dispatch attempt, not before it. Processes run once per sub-command
     * attempted regardless of whether that sub-command succeeded or failed, matching the
     * original per-turn cadence. This is the only entry point; the former run(BufferedReader)
     * console loop was removed with the CLI runner, and runProcesses() is now called here
     * rather than by the caller.
     */
    public CommandOutcome processCommand(String anInput) {
        try {
            CommandSequence sequence = parser.handle(anInput);
            for (GenericCommandDescription command : sequence.commands()) {
                boolean succeeded = runOneCommandSucceeded(command);
                ExecutionResult processesResult = gameContext.runProcesses();
                String processMessage = processesResult.getResultMessage();
                gameContext.tell(processMessage);
                if (!succeeded) {
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
     * Runs one already-parsed sub-command of a (possibly conjunction-joined) turn: tries the
     * current location/pocket commands first (CommandExecutor) and, only if nothing local
     * matched the verb at all (FAILURE carrying the SM8 sentinel), falls back to the workflow
     * Responses (gameContext.respondTo()); then tells the result. Re-reads
     * gameContext.getCurrentLocation()/getPocket() rather than reusing a value captured once
     * for the whole turn, because an earlier sub-command in the same sequence (e.g. "go north")
     * may have moved the player, and this sub-command must see that new location.
     *
     * @return true if this sub-command succeeded and the sequence should continue; false if it
     * failed (or couldn't be understood) and the remaining sub-commands must not be attempted.
     */
    private boolean runOneCommandSucceeded(GenericCommandDescription command) {
        // Continue if the user provided nothing that we understand. A command needs at
        // least a verb to be actionable - e.g. a bare noun like "suit" with no verb - so
        // that alone is unparseable, not merely a command with no matching handler.
        if (VocabularyData.EMPTY_STRING.equals(command.getVerb())) {
            gameContext.tell(SystemMessageKey.SM6.defaultText());
            return false;
        }

        // Check commands that are possible because of the players inventory or the current location.
        CommandExecutor commandExecuter = new CommandExecutor(gameContext.getPocket(), gameContext.getCurrentLocation());
        ExecutionResult result = commandExecuter.execute(command);

        // Nothing local handled this verb - fall back to the workflow Responses (inventory, quit,
        // help, and any the author added).
        if (result.getExecutionState() == ExecutionResult.State.FAILURE &&
            result.getResultMessage().equals(SystemMessageKey.SM8.defaultText())) {
            result = gameContext.respondTo(command);
        }

        if (result.getExecutionState() != ExecutionResult.State.FAILURE) {
            gameContext.tell(result.getResultMessage());
            return true;
        }

        if (!VocabularyData.EMPTY_STRING.equals(result.getResultMessage())) {
            gameContext.tell(result.getResultMessage());
        } else {
            if (result.getExecutionState() == ExecutionResult.State.FAILURE) {
                gameContext.tell(SystemMessageKey.SM8.defaultText());
            } else {
                gameContext.tell(SystemMessageKey.SM15.defaultText());
            }
        }
        return result.getExecutionState() != ExecutionResult.State.FAILURE;
    }
}
