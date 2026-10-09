package com.pdg.adventure.server.engine;

import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.pdg.adventure.api.Command;
import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.parser.GenericCommandChain;
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
 * authoring UI). Each table key maps to a {@link CommandChain} rather than a single {@link
 * Command} - mirroring {@code GenericCommandProvider}'s location/item command tables - so an
 * author can add multiple rows sharing the same verb+adjective+noun (e.g. two "switch lamp"
 * Responses, one gated on a PREPOSITION-"on" condition and one on "off") and have every one of
 * them tried in insertion order until one's preconditions pass, instead of the second row
 * silently overwriting the first.
 */
public class Workflow {

    private static final Comparator<CommandDescription> ALPHABETICAL = Comparator
            .comparing(CommandDescription::getVerb, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(CommandDescription::getAdjective, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(CommandDescription::getNoun, String.CASE_INSENSITIVE_ORDER);

    private final Map<CommandDescription, CommandChain> processes;
    private final Map<CommandDescription, CommandChain> responses;
    private final Map<CommandDescription, CommandChain> arrivalProcesses;
    private boolean runningArrivalProcesses;

    public Workflow() {
        processes = new TreeMap<>();
        responses = new TreeMap<>();
        arrivalProcesses = new TreeMap<>();
    }

    public void addProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        chainFor(processes, aCommandDescription).addCommand(aCommand);
    }

    public void addResponse(GenericCommandDescription aCommandDescription, Command aCommand) {
        chainFor(responses, aCommandDescription).addCommand(aCommand);
    }

    public void addArrivalProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        chainFor(arrivalProcesses, aCommandDescription).addCommand(aCommand);
    }

    public void removeProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        removeFrom(processes, aCommandDescription, aCommand);
    }

    public void removeResponse(GenericCommandDescription aCommandDescription, Command aCommand) {
        removeFrom(responses, aCommandDescription, aCommand);
    }

    public void removeArrivalProcess(GenericCommandDescription aCommandDescription, Command aCommand) {
        removeFrom(arrivalProcesses, aCommandDescription, aCommand);
    }

    private static CommandChain chainFor(Map<CommandDescription, CommandChain> aTable,
            GenericCommandDescription aCommandDescription) {
        return aTable.computeIfAbsent(aCommandDescription, _ -> new GenericCommandChain());
    }

    private static void removeFrom(Map<CommandDescription, CommandChain> aTable,
            GenericCommandDescription aCommandDescription, Command aCommand) {
        CommandChain chain = aTable.get(aCommandDescription);
        if (chain != null) {
            chain.removeCommand(aCommand);
        }
    }

    public ExecutionResult runProcesses() {
        return getExecutionResult(processes);
    }

    /**
     * Not re-entrant: an Arrival Process that itself describes the location (a Look action) would otherwise fire
     * the Arrival Processes again, without end. A nested call returns an empty result instead.
     */
    public ExecutionResult runArrivalProcesses() {
        if (runningArrivalProcesses) {
            return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        }
        runningArrivalProcesses = true;
        try {
            return getExecutionResult(arrivalProcesses);
        } finally {
            runningArrivalProcesses = false;
        }
    }

    @NonNull
    private ExecutionResult getExecutionResult(final Map<CommandDescription, CommandChain> someProcesses) {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        someProcesses.entrySet().stream()
                         .sorted(Map.Entry.comparingByKey(ALPHABETICAL))
                         .forEach(commandEntry -> {
                    ExecutionResult innerResult = commandEntry.getValue().execute();
                    String innerMessage = innerResult.getResultMessage();
                    if (!innerMessage.isEmpty()) {
                        result.setResultMessage((result.getResultMessage() +
                                                " " +
                                                innerMessage).trim());
                    }
                });
        return result;
    }

    public ExecutionResult respondTo(CommandDescription aCommand) {
        ExecutionResult result = new CommandExecutionResult();
        CommandChain chain = findResponseChain(aCommand);
        if (chain != null) {
            result = chain.execute();
        }
        return result;
    }

    // An exact verb+adjective+noun response always wins. Failing that, a response whose noun is
    // the wildcard (VocabularyData.WILDCARD_NOUN) answers for any noun - including none, which is
    // what the parser hands over when the player typed a word it doesn't know (it drops those).
    // A wildcard response for the same adjective is preferred over one without an adjective.
    private CommandChain findResponseChain(CommandDescription aCommand) {
        CommandChain chain = responses.get(aCommand);
        if (chain == null) {
            chain = responses.get(new GenericCommandDescription(aCommand.getVerb(), aCommand.getAdjective(),
                                                                VocabularyData.WILDCARD_NOUN));
        }
        if (chain == null) {
            chain = responses.get(new GenericCommandDescription(aCommand.getVerb(), VocabularyData.EMPTY_STRING,
                                                                VocabularyData.WILDCARD_NOUN));
        }
        return chain;
    }
}
