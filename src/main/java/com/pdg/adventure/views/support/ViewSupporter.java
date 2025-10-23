package com.pdg.adventure.views.support;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.Optional;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.views.components.VocabularyPicker;
import com.pdg.adventure.views.locations.LocationDescriptionAdapter;
import com.pdg.adventure.views.locations.LocationViewModel;

public class ViewSupporter {

    public static String formatId(Ided anIdedData) {
        return formatId(anIdedData.getId().substring(0, 8));
    }

    public static String formatId(String anIdedData) {
        return anIdedData.substring(0, 8);
    }

    public static void populateStartLocation(AdventureData anAdventureData, TextField aStartLocation) {
        final LocationData locationById = anAdventureData.getLocationData().get(anAdventureData.getCurrentLocationId());
        if (locationById != null) {
            aStartLocation.setValue(getLocationsShortedDescription(locationById));
        }
    }

    public static String getLocationsShortedDescription(LocationData location) {
        if (location == null) {
            return "";
        }
        final DescriptionData locationDescriptionData = location.getDescriptionData();
        Word nounWord = locationDescriptionData.getNoun();
        String noun = nounWord.getText();

        Word adjectiveWord = locationDescriptionData.getAdjective();
        String adjective = adjectiveWord == null ? null : adjectiveWord.getText();

        if (noun == null || noun.isEmpty()) {
            return locationDescriptionData.getShortDescription();
        }
        return getShortDescription(noun, adjective);
    }

    public static String getLocationsShortedDescription(LocationDescriptionAdapter location) {
        if (location == null) {
            return "";
        }
        final String noun = location.getNoun();
        if (noun == null || noun.isEmpty()) {
            return location.getShortDescription();
        }
        final String adjective = location.getAdjective();
        return getShortDescription(noun, adjective);
    }

    private static String getShortDescription(String noun, String adjective) {
        if (noun == null || noun.isEmpty()) {
            return "";
        }
        if (adjective == null || adjective.isEmpty()) {
            return noun;
        }

        String shortDescription = noun + " / " + adjective;
        if (shortDescription.length() > 20) {
            shortDescription = shortDescription.substring(0, 20) + "...";
        }
        return shortDescription;
    }

    public static String formatDescription(ItemData anItem) {
        return formatDescription(anItem.getDescriptionData());
    }

    public static String formatDescription(CommandDescriptionData aCommandDescription) {
        if (aCommandDescription == null) {
            return "";
        }
        String noun = getWordText(aCommandDescription.getNoun());
        String adjective = getWordText(aCommandDescription.getAdjective());
        String verb = getWordText(aCommandDescription.getVerb());

        // Combine non-empty parts
        StringBuilder command = new StringBuilder();
        if (!verb.isEmpty()) {
            command.append(verb);
        }
        if (!adjective.isEmpty()) {
            if (command.length() > 0) {
                command.append(" ");
            }
            command.append(adjective);
        }
        if (!noun.isEmpty()) {
            if (command.length() > 0) {
                command.append(" ");
            }
            command.append(noun);
        }
        return command.toString();
    }

    public static String formatDescription(Describable aDescribable) {
        String shortDescription = aDescribable.getShortDescription();
        if (shortDescription.isEmpty()) {
            shortDescription = aDescribable.getNoun() + " / " + aDescribable.getAdjective();
        }
        return shortDescription;
    }

    public static String formatDescription(DescriptionData aDescriptionData) {
        String shortDescription = aDescriptionData.getShortDescription();
        if (shortDescription.isEmpty()) {
            shortDescription = getWordText(aDescriptionData.getNoun()) + " / " + getWordText(aDescriptionData.getAdjective());
        }
        return shortDescription;
    }

    public static String formatDescription(LocationData aLocationData) {
        return formatDescription(aLocationData.getDescriptionData());
    }

    public static void bindField(Binder<DirectionData> binder, ComboBox<String> field, VocabularyData aVocabulary,
                                 Word.Type type, CommandDescriptionData commandDescriptionData) {
        binder.forField(field).bind(directionData -> {
            return getWordText(getWord(commandDescriptionData, type));
        }, (directionData, word) -> {
            setWord(aVocabulary, word, commandDescriptionData);
        });
    }

    public static void bindField(Binder<DirectionData> aBinder, VocabularyPicker aVocabularyPicker,
                                 Word.Type type, CommandDescriptionData aCommandDescriptionData) {
        /*******************************************/
//        aBinder.bind(aVocabularyPicker, (directionData) -> {
//            return aVocabularyPicker.getValue();
//        }, (directionData, word) -> {
//            setWord(aCommandDescriptionData, aVocabularyPicker.getValue());
//        });
        /*******************************************/
    }


    public static void setWord(VocabularyData aVocabulary, String aWordText,
                               CommandDescriptionData aCommandDescriptionData) {
        if (aWordText == null || aWordText.isEmpty()) {
            return;
        }
        Optional<Word> word = resolveWord(aVocabulary, aWordText);
        if (word.isEmpty()) {
            return;
        }
        Word foundWord = word.get();
        setWord(aCommandDescriptionData, foundWord);
    }

    private static void setWord(CommandDescriptionData aCommandDescriptionData, Word foundWord) {
        switch (foundWord.getType()) {
            case NOUN -> aCommandDescriptionData.setNoun(foundWord);
            case ADJECTIVE -> aCommandDescriptionData.setAdjective(foundWord);
            case VERB -> aCommandDescriptionData.setVerb(foundWord);
        }
    }

    private static Optional<Word> resolveWord(VocabularyData aVocabulary, String aWordText) {
        Optional<Word> resolvedWord = aVocabulary.findWord(aWordText);
        return resolvedWord;
    }

    public static Word getWord(CommandDescriptionData commandDescriptionData, Word.Type aWordType) {
        Word word;
        switch (aWordType) {
            case NOUN -> word = commandDescriptionData.getNoun();
            case ADJECTIVE -> word = commandDescriptionData.getAdjective();
            case VERB -> word = commandDescriptionData.getVerb();
            default -> throw new IllegalStateException("Unexpected value: " + aWordType);
        }
        return word;
    }

    public static String getWordText(Word aWord) {
        return aWord == null ? "" : aWord.getText();
    }


    public static void bindField(Binder<LocationViewModel> locationDataBinder, TextField aField,
                                 VocabularyData aVocabulary, Word.Type aType, DescriptionData aDescriptionData) {
        locationDataBinder.bind(aField, locationData -> {
            return getWordText(getWord(aDescriptionData, aType));
        }, (locationData, word) -> {
            setWord(aVocabulary, word, aType, aDescriptionData);
        });
    }

    private static void setWord(VocabularyData aVocabulary, String aWordText, Word.Type aType,
                                DescriptionData aDescriptionData) {
        Optional<Word> word = resolveWord(aVocabulary, aWordText);
        if (word.isEmpty()) {
            return;
        }
        Word foundWord = word.get();
        switch (aType) {
            case NOUN -> aDescriptionData.setNoun(foundWord);
            case ADJECTIVE -> aDescriptionData.setAdjective(foundWord);
        }
    }

    private static Word getWord(DescriptionData aDescriptionData, Word.Type aType) {
        Word word;
        switch (aType) {
            case NOUN -> word = aDescriptionData.getNoun();
            case ADJECTIVE -> word = aDescriptionData.getAdjective();
            default -> throw new IllegalStateException("Unexpected value: " + aType);
        }
        return word;
    }

    public static void bindField(Binder<LocationViewModel> aBinder, ComboBox<Word> aWord, Word.Type aType) {
        switch (aType) {
            case NOUN -> aBinder.bind(aWord, LocationViewModel::getNoun, LocationViewModel::setNoun);
            case ADJECTIVE -> aBinder.bind(aWord, LocationViewModel::getAdjective, LocationViewModel::setAdjective);
        }
    }
}
