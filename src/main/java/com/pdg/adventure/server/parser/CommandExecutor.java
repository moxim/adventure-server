package com.pdg.adventure.server.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

public class CommandExecutor {

    private final Actionable pocket;
    private final Actionable location;

    public CommandExecutor(Actionable aPocket, Actionable aLocation) {
        pocket = aPocket;
        location = aLocation;
    }

    public static ExecutionResult clarifyExecutionOutcome(ExecutionResult result) {
        if (ExecutionResult.State.FAILURE == result.getExecutionState()) {
            if (VocabularyData.EMPTY_STRING.equals(result.getResultMessage())) {
                result.setResultMessage(SystemMessageKey.SM8.defaultText());
            }
        } else if (ExecutionResult.State.SUCCESS == result.getExecutionState()) {
            result.setResultMessage(SystemMessageKey.SM15.defaultText());
        }

        return result;
    }

    public ExecutionResult execute(CommandDescription aCommand) {
        // first look for commands in the pocket
        List<CommandChain> availableCommandChains = pocket.getMatchingCommandChain(aCommand);
        // then add commands in the location itself, the directions and the items in the location
        availableCommandChains.addAll(location.getMatchingCommandChain(aCommand));

        reduceCommandChains(availableCommandChains, aCommand);

        ExecutionResult result = new CommandExecutionResult();
        if (commandCanBeExecuted(availableCommandChains, result, aCommand.getNoun(), aCommand.getVerb())) {
            result = availableCommandChains.getFirst().execute();
            return result;
        }

        return clarifyExecutionOutcome(result);
    }

    private void reduceCommandChains(List<CommandChain> availableCommandChains, CommandDescription aCommand) {
        availableCommandChains.removeIf(c -> c.getCommands().isEmpty());
        reduceByAdjective(availableCommandChains, aCommand);
        reduceByNoun(availableCommandChains, aCommand);
        reduceToBestRankedChains(availableCommandChains);
    }

    private void reduceByAdjective(List<CommandChain> availableCommandChains, CommandDescription aCommand) {
        // GenericCommandProvider treats EMPTY_STRING as a wildcard, so a chain whose own
        // adjective is empty is a valid match for any input adjective. Drop chains with a
        // non-empty, non-matching adjective. Then, if any exact-adjective match remains,
        // also drop the wildcards so the engine can pick the specific one unambiguously.
        String cmdAdj = aCommand.getAdjective();
        if (VocabularyData.EMPTY_STRING.equals(cmdAdj)) {
            return;
        }
        availableCommandChains.removeIf(c -> {
            String adj = c.getCommands().getFirst().getDescription().getAdjective();
            return !VocabularyData.EMPTY_STRING.equals(adj) && !adj.equals(cmdAdj);
        });
        boolean hasExactMatch = availableCommandChains.stream().anyMatch(c ->
                cmdAdj.equals(c.getCommands().getFirst().getDescription().getAdjective()));
        if (hasExactMatch) {
            availableCommandChains.removeIf(c ->
                    VocabularyData.EMPTY_STRING.equals(c.getCommands().getFirst().getDescription().getAdjective()));
        }
    }

    // GenericCommandProvider treats an empty stored noun as a wildcard, so e.g. a bare "jump"
    // chain also matches a query of "jump sea". That wildcard chain must always lose to a chain
    // whose own noun matches the query exactly - regardless of which of the specific chain's own
    // commands currently applies - the same specificity-over-wildcard preference reduceByAdjective
    // already applies for adjective. Without this, an unconditional wildcard fallback (or one
    // whose own command has no discriminating precondition) can outrank a more specific chain
    // whose only currently-satisfied command happens to be an excuse (e.g. "jump sea" without the
    // wetsuit correctly explaining why, instead of silently falling back to a bare "jump").
    private void reduceByNoun(List<CommandChain> availableCommandChains, CommandDescription aCommand) {
        String cmdNoun = aCommand.getNoun();
        if (VocabularyData.EMPTY_STRING.equals(cmdNoun)) {
            return;
        }
        boolean hasExactMatch = availableCommandChains.stream().anyMatch(c ->
                cmdNoun.equals(c.getCommands().getFirst().getDescription().getNoun()));
        if (hasExactMatch) {
            availableCommandChains.removeIf(c ->
                    VocabularyData.EMPTY_STRING.equals(c.getCommands().getFirst().getDescription().getNoun()));
        }
    }

    // When the same trigger text matches commands on more than one candidate (e.g. two items
    // sharing a noun, or a generic fallback chain overlapping a more specific one via the
    // empty-noun wildcard), rank each by how strongly its OWN state-dependent preconditions
    // currently back it, and keep only the best-ranked tier:
    //   1 (best):  has a currently-satisfied, precondition-gated command with a real action -
    //              e.g. "jump sea" while wearing the wetsuit: the move actually applies.
    //   2 (middle): neither of the others - the chain's outcome doesn't depend on any
    //              precondition that currently discriminates it. Covers both a "real" branch
    //              with no precondition of its own (drop's success command has no
    //              CarriedCondition - see ItemEditorView.createPickupCommands) and a fully
    //              unconditional fallback chain (a bare unqualified "jump").
    //   3 (worst): has a currently-satisfied, precondition-gated command whose only actions are
    //              informational - e.g. "you don't have a suit": an explicit, state-dependent
    //              reason this candidate is wrong.
    // A precondition-less command carries no discriminating information regardless of whether
    // its actions are real or informational, so it never affects the rank - this is what stops
    // an unconditional "also here" flavour message (jump-sea's third command) from being
    // mistaken for a competing excuse against the wildcard "jump" chain's own unconditional
    // message. Leaves the list untouched when every candidate ranks equally (genuine ambiguity).
    private void reduceToBestRankedChains(List<CommandChain> availableCommandChains) {
        if (availableCommandChains.size() <= 1) {
            return;
        }
        Map<CommandChain, Integer> rankByChain = new HashMap<>();
        for (CommandChain chain : availableCommandChains) {
            rankByChain.put(chain, rankOf(chain));
        }
        int bestRank = availableCommandChains.stream().mapToInt(rankByChain::get).min().orElseThrow();
        List<CommandChain> best = availableCommandChains.stream()
                .filter(chain -> rankByChain.get(chain) == bestRank)
                .toList();
        if (best.size() < availableCommandChains.size()) {
            availableCommandChains.retainAll(best);
        }
    }

    private static int rankOf(CommandChain aChain) {
        boolean hasGatedRealAction = false;
        boolean hasGatedExcuse = false;
        for (Command command : aChain.getCommands()) {
            if (command.getPreconditions().isEmpty() || !preconditionsCurrentlyHold(command)
                    || command.getActions().isEmpty()) {
                continue;
            }
            if (command.getActions().stream().allMatch(Action::isInformationalOnly)) {
                hasGatedExcuse = true;
            } else {
                hasGatedRealAction = true;
            }
        }
        if (hasGatedRealAction) {
            return 1;
        }
        return hasGatedExcuse ? 3 : 2;
    }

    // Only safe to call for a dry-run decision (as opposed to real execution) when every
    // precondition is deterministic - a non-deterministic one (e.g. ChanceCondition) is skipped
    // entirely rather than rolled here, so this never consumes/pre-empts the roll chain.execute()
    // will make moments later; such a command is simply never treated as "currently applies".
    private static boolean preconditionsCurrentlyHold(Command aCommand) {
        List<PreCondition> preconditions = aCommand.getPreconditions();
        if (preconditions.stream().anyMatch(condition -> !condition.isDeterministic())) {
            return false;
        }
        return preconditions.stream()
                .allMatch(condition -> condition.check().getExecutionState() == ExecutionResult.State.SUCCESS);
    }

    private boolean commandCanBeExecuted(List<CommandChain> availableCommandChains, ExecutionResult result,
                                         String aNoun, String aVerb) {
        if (availableCommandChains.isEmpty()) {
            result.setResultMessage(SystemMessageKey.SM8.defaultText());
            return false;
        } else if (availableCommandChains.size() > 1) {
            if (VocabularyData.EMPTY_STRING.equals(aNoun)) {
                result.setResultMessage(SystemMessageKey.SM60.defaultText().formatted(aVerb));
            } else {
                result.setResultMessage(SystemMessageKey.SM61.defaultText().formatted(aNoun, aVerb));
            }
            return false;
        }
        return true;
    }
}
