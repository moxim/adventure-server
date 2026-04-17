package com.pdg.adventure.server.parser;

import lombok.Getter;

import java.util.*;

import com.pdg.adventure.api.*;
import com.pdg.adventure.model.VocabularyData;

public class GenericCommandProvider implements CommandProvider {
    @Getter
    private final Map<CommandDescription, CommandChain> availableCommands;
    private String id;

    public GenericCommandProvider() {
        availableCommands = new HashMap<>();
        id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
    }

    @Override
    public void addCommand(Command aCommand) {
        CommandChain chain = availableCommands.get(aCommand.getDescription());
        if (chain == null) {
            chain = new GenericCommandChain();
        }
        chain.addCommand(aCommand);
        availableCommands.put(aCommand.getDescription(), chain);
    }

    @Override
    public void removeCommand(Command aCommand) {
        availableCommands.remove(aCommand.getDescription());
    }

    public boolean hasCommand(GenericCommandDescription aCommand) {
        return availableCommands.containsKey(aCommand);
    }

    @Override
    public List<Command> getCommands() {
        List<Command> commands = new ArrayList<>();
        for (CommandChain chain : availableCommands.values()) {
            commands.addAll(chain.getCommands());
        }
        return commands;
    }

    public ExecutionResult applyCommand(CommandDescription aCommandDescription) {
        ExecutionResult result = new CommandExecutionResult();
        List<CommandChain> commandChain = getMatchingCommandChain(aCommandDescription);
        for (CommandChain chain : commandChain) {
            result = chain.execute();
            result.setCommandHasMatched();
        }
        return result;
    }

    @Override
    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        String verb = aCommandDescription.getVerb();
        String adjective = aCommandDescription.getAdjective();
        String noun = aCommandDescription.getNoun();

        // noun must match exactly; verb/adjective match when either side equals the other
        // or either side is VocabularyData.EMPTY_STRING (wildcard). Returns all matching chains.
        List<CommandChain> result = new ArrayList<>();
        for (Map.Entry<CommandDescription, CommandChain> entry : availableCommands.entrySet()) {
            CommandDescription itemCommand = entry.getKey();
            String itemVerb = itemCommand.getVerb();
            String itemAdjective = itemCommand.getAdjective();
            String itemNoun = itemCommand.getNoun();

            // noun must match exactly
            if (!Objects.equals(itemNoun, noun)) {
                continue;
            }

            // treat null as EMPTY_STRING just in case
            String inVerb = verb == null ? VocabularyData.EMPTY_STRING : verb;
            String inAdj = adjective == null ? VocabularyData.EMPTY_STRING : adjective;
            String itVerb = itemVerb == null ? VocabularyData.EMPTY_STRING : itemVerb;
            String itAdj = itemAdjective == null ? VocabularyData.EMPTY_STRING : itemAdjective;

            boolean verbMatches = itVerb.equals(inVerb)
                    || VocabularyData.EMPTY_STRING.equals(itVerb)
                    || VocabularyData.EMPTY_STRING.equals(inVerb);

            boolean adjMatches = itAdj.equals(inAdj)
                    || VocabularyData.EMPTY_STRING.equals(itAdj)
                    || VocabularyData.EMPTY_STRING.equals(inAdj);

            if (verbMatches && adjMatches) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public boolean hasVerb(String aVerb) {
        for (CommandDescription description : availableCommands.keySet()) {
            if (description.getVerb().equals(aVerb)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "GenericCommandProvider{" +
               "availableCommands=" + availableCommands +
               ", id='" + id + '\'' +
               '}';
    }
}
