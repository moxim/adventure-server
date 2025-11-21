package com.pdg.adventure.view.vocabulary;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.*;

public class WordUsageTracker {

    private static final String DIRECTION_TEXT = "Direction";
    private final AdventureData adventureData;
    private final VocabularyData vocabularyData;

    public WordUsageTracker(AdventureData adventureData, VocabularyData vocabularyData) {
        this.adventureData = adventureData;
        this.vocabularyData = vocabularyData;
    }

    List<WordUsage> getAllWordUsages(Word targetWord) {
        List<WordUsage> usages = new ArrayList<>();
        if (adventureData == null || targetWord == null) {
            return usages;
        }

        // Check if this word is the take or drop verb
        if (vocabularyData != null) {
            if (vocabularyData.getTakeWord() != null &&
                vocabularyData.getTakeWord().getId().equals(targetWord.getId())) {
                usages.add(new WordUsage("Take Verb", "Special verb for picking up items", "Special"));
            }
            if (vocabularyData.getDropWord() != null &&
                vocabularyData.getDropWord().getId().equals(targetWord.getId())) {
                usages.add(new WordUsage("Drop Verb", "Special verb for dropping items", "Special"));
            }

            // Check synonyms
            for (Word word : vocabularyData.getWords()) {
                if (word.getSynonym() != null && word.getSynonym().getId().equals(targetWord.getId())) {
                    usages.add(new WordUsage("Synonym", word.getText() + " (" + word.getType() + ")", "Word"));
                }
            }
        }

        usages.addAll(checkWordIsNotUsedInLocations(targetWord));

        return usages;
    }

    List<WordUsage> checkWordIsNotUsedInLocations(final Word targetWord) {
        List<WordUsage> usages = new ArrayList<>();
        // Check locations
        if (adventureData.getLocationData() != null) {
            for (LocationData location : adventureData.getLocationData().values()) {
                checkDescriptionUsage(location.getDescriptionData(), targetWord, "Location", location.getId(), usages);
                checkCommandProviderUsage(location.getCommandProviderData(), targetWord, "Location", location.getId(),
                                          usages);

                // Check directions in this location
                if (location.getDirectionsData() != null) {
                    checkWordIsNotUsedInDirectionsOfLocation(targetWord, location, usages);
                }

                // Check items in this location
                if (location.getItemContainerData() != null && location.getItemContainerData().getItems() != null) {
                    checkWordIsNotUsedInItemsOfLocation(targetWord, location, usages);
                }
            }
        }
        return usages;
    }

    private void checkWordIsNotUsedInItemsOfLocation(final Word targetWord, final LocationData location,
                                                     final List<WordUsage> usages) {
        for (ItemData item : location.getItemContainerData().getItems()) {
            if (item != null) {
                checkDescriptionUsage(item.getDescriptionData(), targetWord, "Item", item.getId(), usages);
                checkCommandProviderUsage(item.getCommandProviderData(), targetWord, "Item", item.getId(),
                                          usages);
            }
        }
    }

    private void checkWordIsNotUsedInDirectionsOfLocation(final Word targetWord, final LocationData location,
                                                          final List<WordUsage> usages) {
        for (DirectionData direction : location.getDirectionsData()) {
            checkDescriptionUsage(direction.getDescriptionData(), targetWord, DIRECTION_TEXT, direction.getId(),
                                  usages);
            checkCommandProviderUsage(direction.getCommandProviderData(), targetWord, DIRECTION_TEXT, direction.getId(),
                                      usages);
            checkCommandDataUsage(direction.getCommandData(), targetWord, DIRECTION_TEXT, direction.getId(),
                                  usages);
        }
    }

    private void checkDescriptionUsage(com.pdg.adventure.model.basic.DescriptionData description, Word targetWord,
                                       String type, String id, List<WordUsage> usages) {
        if (description == null) return;

        if (description.getAdjective() != null && description.getAdjective().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Adjective", id, type));
        }
        if (description.getNoun() != null && description.getNoun().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Noun", id, type));
        }
    }

    private void checkCommandProviderUsage(CommandProviderData commandProvider, Word targetWord, String type, String id,
                                           List<WordUsage> usages) {
        if (commandProvider == null || commandProvider.getAvailableCommands() == null) return;

        for (CommandChainData commandChain : commandProvider.getAvailableCommands().values()) {
            if (commandChain != null && commandChain.getCommands() != null) {
                for (CommandData command : commandChain.getCommands()) {
                    checkCommandDataUsage(command, targetWord, type, id, usages);
                }
            }
        }
    }

    private void checkCommandDataUsage(CommandData command, Word targetWord, String type, String id,
                                       List<WordUsage> usages) {
        if (command == null || command.getCommandDescription() == null) return;

        com.pdg.adventure.model.basic.CommandDescriptionData cmdDesc = command.getCommandDescription();
        if (cmdDesc.getVerb() != null && cmdDesc.getVerb().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Verb in Command", id, type));
        }
        if (cmdDesc.getAdjective() != null && cmdDesc.getAdjective().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Adjective in Command", id, type));
        }
        if (cmdDesc.getNoun() != null && cmdDesc.getNoun().getId().equals(targetWord.getId())) {
            usages.add(new WordUsage("Noun in Command", id, type));
        }
    }

    StringBuilder createUsagesText(final List<WordUsage> usages, int maxUsagesToShow) {
        // Build the message with grouped usages
        StringBuilder message = new StringBuilder();
        message.append("Found ").append(usages.size()).append(" usage(s):\n\n");

        // Group usages by type
        java.util.Map<String, List<WordUsage>> groupedUsages = new java.util.HashMap<>();

        for (var i = 0; i < Math.min(usages.size(), Math.max(usages.size(), maxUsagesToShow)); i++) {
            WordUsage usage = usages.get(i);
            String key = usage.itemType;
            groupedUsages.computeIfAbsent(key, k -> new ArrayList<>()).add(usage);
        }

        // Display grouped usages
        for (String groupType : groupedUsages.keySet()) {
            message.append("▶ ").append(groupType).append("s:\n");
            for (WordUsage usage : groupedUsages.get(groupType)) {
                message.append("  • ").append(usage.itemId);
                if (!usage.usageType.equals(groupType)) {
                    message.append(" (").append(usage.usageType).append(")");
                }
                message.append("\n");
            }
            message.append("\n");
        }
        return message;
    }

}
