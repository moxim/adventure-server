package com.pdg.adventure.server.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.VocabularyData;

/**
 * Reusable command-handling component. Wraps a {@link GenericCommandProvider} and exposes
 * the full {@link HasCommands} contract. Both {@code Thing} and {@code GenericDirection}
 * compose an instance of this class rather than duplicating the delegation boilerplate.
 */
public class CommandHandler implements HasCommands {

    private GenericCommandProvider commandProvider;
    private String examineFallbackVerb;
    private Supplier<String> examineFallbackDescription;
    private Supplier<String> examineFallbackOwnerNoun;
    private Supplier<String> examineFallbackOwnerAdjective;

    public CommandHandler() {
        commandProvider = new GenericCommandProvider();
    }

    public void setExamineFallback(String aVerb, Supplier<String> aDescription) {
        setExamineFallback(aVerb, aDescription,
                           () -> VocabularyData.EMPTY_STRING,
                           () -> VocabularyData.EMPTY_STRING);
    }

    public void setExamineFallback(String aVerb, Supplier<String> aDescription,
                                   Supplier<String> anOwnerNoun, Supplier<String> anOwnerAdjective) {
        examineFallbackVerb = aVerb;
        examineFallbackDescription = aDescription;
        examineFallbackOwnerNoun = anOwnerNoun;
        examineFallbackOwnerAdjective = anOwnerAdjective;
    }

    @Override
    public void addCommand(Command aCommand) {
        commandProvider.addCommand(aCommand);
    }

    @Override
    public void removeCommand(Command aCommand) {
        commandProvider.removeCommand(aCommand);
    }

    @Override
    public boolean hasVerb(String aVerb) {
        return commandProvider.hasVerb(aVerb);
    }

    @Override
    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        List<CommandChain> result = commandProvider.getMatchingCommandChain(aCommandDescription);
        if (result.isEmpty()
                && examineFallbackVerb != null
                && examineFallbackVerb.equals(aCommandDescription.getVerb())
                && examineFallbackDescription != null
                && ownerNounMatches(aCommandDescription.getNoun())
                && ownerAdjectiveMatches(aCommandDescription.getAdjective())) {
            GenericCommandChain chain = new GenericCommandChain();
            Supplier<String> description = examineFallbackDescription;
            final GenericCommand describeCommand = new GenericCommand(
                    new GenericCommandDescription(examineFallbackVerb),
                    new ExamineFallbackAction(description));
            describeCommand.setId("examineFallback");
            chain.addCommand(describeCommand);
            result = new ArrayList<>(List.of(chain));
        }
        return result;
    }

    @Override
    public ExecutionResult applyCommand(CommandDescription aCommandDescription) {
        ExecutionResult result = new CommandExecutionResult();
        for (CommandChain chain : getMatchingCommandChain(aCommandDescription)) {
            result = chain.execute();
            result.setCommandHasMatched();
        }
        return result;
    }

    @Override
    public List<Command> getCommands() {
        return commandProvider.getCommands();
    }

    @Override
    public GenericCommandProvider getCommandProvider() {
        return commandProvider;
    }

    public void setCommandProvider(GenericCommandProvider aCommandProvider) {
        commandProvider = aCommandProvider;
    }

    private boolean ownerNounMatches(String aCommandNoun) {
        String cmdNoun = aCommandNoun == null ? VocabularyData.EMPTY_STRING : aCommandNoun;
        if (VocabularyData.EMPTY_STRING.equals(cmdNoun)) {
            return true;
        }
        String ownerNoun = examineFallbackOwnerNoun == null ? VocabularyData.EMPTY_STRING : examineFallbackOwnerNoun.get();
        return cmdNoun.equals(ownerNoun);
    }

    private boolean ownerAdjectiveMatches(String aCommandAdjective) {
        String cmdAdj = aCommandAdjective == null ? VocabularyData.EMPTY_STRING : aCommandAdjective;
        if (VocabularyData.EMPTY_STRING.equals(cmdAdj)) {
            return true;
        }
        String ownerAdj = examineFallbackOwnerAdjective == null ? VocabularyData.EMPTY_STRING : examineFallbackOwnerAdjective.get();
        return cmdAdj.equals(ownerAdj);
    }
}
