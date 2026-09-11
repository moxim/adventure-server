package com.pdg.adventure.server.engine;

import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandDescription;

/**
 * An adventure's global commands, held as three tables:
 * <ul>
 *   <li><b>{@code processes}</b> — run automatically after every parsed sub-command,
 *       regardless of what the player typed ({@link #runProcesses()}).</li>
 *   <li><b>{@code arrivalProcesses}</b> — run automatically whenever the current location's
 *       description is (re)shown: on arrival via movement, and on an explicit look/describe
 *       ({@link #runArrivalProcesses()}). Re-fires on every redescribe of the same location by
 *       design (see docs/superpowers/specs/2026-09-11-process-arrival-timing-design.md); an
 *       author who wants "only once" adds their own guard condition. Output order: this method
 *       returns the matched entries' joined message rather than telling it itself — the caller
 *       (e.g. {@code MovePlayerAction}) appends it after its own message (the destination's
 *       arrival description), so the description prints first and arrival-process text follows
 *       it, same ordering as {@code processes}.</li>
 *   <li><b>{@code responses}</b> — a fallback table, consulted by {@link #respondTo} only
 *       when no location/pocket command matched the typed verb. Keyed by exact
 *       {@link CommandDescription}.</li>
 * </ul>
 * The domain terms are <i>Processes</i>, <i>Arrival Processes</i>, and <i>Responses</i> (see the
 * authoring UI).
 */
public class Workflow {

    // Iteration order for processes before each sub-command: alphabetical by verb, then adjective,
    // then noun - independent of the TreeMap's own key ordering (which sorts by the
    // "verb|adjective|noun" description string and, because '|' sorts after letters, would rank
    // e.g. "go" after "goto").
    private static final Comparator<CommandDescription> ALPHABETICAL = Comparator
            .comparing(CommandDescription::getVerb, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(CommandDescription::getAdjective, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(CommandDescription::getNoun, String.CASE_INSENSITIVE_ORDER);

    private final Map<CommandDescription, Command> processes;
    private final Map<CommandDescription, Command> responses;
    private final Map<CommandDescription, Command> arrivalProcesses;
    private final GameContext gameContext;

    public Workflow(GameContext aGameContext) {
        processes = new TreeMap<>();
        responses = new TreeMap<>();
        arrivalProcesses = new TreeMap<>();
        gameContext = aGameContext;
    }

    public void addProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        processes.put(aCommandDescription, aCommand);
    }

    public void addResponse(GenericCommandDescription aCommandDescription, Command aCommand) {
        responses.put(aCommandDescription, aCommand);
    }

    public void addArrivalProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        arrivalProcesses.put(aCommandDescription, aCommand);
    }

    public void removeProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        processes.remove(aCommandDescription, aCommand);
    }

    public void removeResponse(GenericCommandDescription aCommandDescription, Command aCommand) {
        responses.remove(aCommandDescription, aCommand);
    }

    public void removeArrivalProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        arrivalProcesses.remove(aCommandDescription, aCommand);
    }

    public ExecutionResult runProcesses() {
        return getExecutionResult(processes);
    }

    public ExecutionResult runArrivalProcesses() {
        return getExecutionResult(arrivalProcesses);
    }

    @NonNull
    private ExecutionResult getExecutionResult(final Map<CommandDescription, Command> someProcesses) {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        someProcesses.entrySet().stream()
                         .sorted(Map.Entry.comparingByKey(ALPHABETICAL))
                         .forEach(commandEntry -> {
                    ExecutionResult innerResult = commandEntry.getValue().execute();
                    String innerMessage = innerResult.getResultMessage();
                    if (!innerMessage.isEmpty()) {
                        result.setResultMessage(result.getResultMessage() +
//                                                "\n" +
                                                innerMessage);
                    }
                });
        return result;
    }

    public ExecutionResult respondTo(CommandDescription aCommand) {
        ExecutionResult result = new CommandExecutionResult();
        Command command = responses.get(aCommand);
        if (command != null) {
            result = command.execute();
        }
        return result;
    }
}
