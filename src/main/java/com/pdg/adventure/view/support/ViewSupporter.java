package com.pdg.adventure.view.support;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.*;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.view.component.VocabularyPicker;
import com.pdg.adventure.view.item.ItemLocationPair;
import com.pdg.adventure.view.location.LocationDescriptionAdapter;
import com.pdg.adventure.view.location.LocationViewModel;

public class ViewSupporter {

    public static int MAX_TEXT_IN_GRID = 32;
    public static int MAX_ID_LENGTH = 26;

    public static UserData getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserData userData) {
            return userData;
        }
        throw new IllegalStateException("No authenticated user in security context");
    }
    
    public static String formatId(Ided anIdedData) {
        if (anIdedData == null) {
            return "";
        }
        String id = anIdedData.getId();
        if (id == null || id.isEmpty()) {
            return "";
        }
        return formatId(id);
    }

    public static String formatId(String anIdedData) {
        return anIdedData.substring(0, Math.min(MAX_ID_LENGTH, anIdedData.length()));
    }

    public static List<ItemLocationPair> getItemLocationPairs(final Collection<LocationData> aLocationDataCollection) {
        // Collect all items from all locations
        List<ItemLocationPair> itemPairs = new ArrayList<>();
        for (LocationData location : aLocationDataCollection) {
            if (location.getItemContainerData() != null) {
                List<ItemData> items = location.getItemContainerData().getItems();
                if (items != null) {
                    for (ItemData item : items) {
                        // Filter out null items (can occur if @DBRef fails to resolve)
                        if (item != null) {
                            itemPairs.add(new ItemLocationPair(item, location));
                        }
                    }
                }
            }
        }
        return itemPairs;
    }

    public static List<ItemData> collectAllItems(AdventureData adventureData) {
        List<ItemData> allItems = new ArrayList<>();
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                List<ItemData> items = location.getItemContainerData().getItems();
                if (items != null) {
                    // Filter out null elements (can occur if @DBRef fails to resolve)
                    items.stream().filter(item -> item != null).forEach(allItems::add);
                }
            }
        }
        if (adventureData.getPlayerPocket() != null) {
            List<ItemData> pocketItems = adventureData.getPlayerPocket().getItems();
            if (pocketItems != null) {
                pocketItems.stream().filter(item -> item != null).forEach(allItems::add);
            }
        }
        return allItems;
    }

    public static List<ItemContainerData> collectAllContainers(AdventureData adventureData) {
        List<ItemContainerData> allContainers = new ArrayList<>();
        if (adventureData.getPlayerPocket() != null) {
            allContainers.add(adventureData.getPlayerPocket());
        }
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                allContainers.add(location.getItemContainerData());
            }
        }
        return allContainers;
    }

    /**
     * A small, muted hint telling authors that grid rows are opened for editing by double-clicking.
     * Placed near each menu grid so the (otherwise hidden) double-click gesture is discoverable.
     */
    public static Span doubleClickEditHint() {
        Span hint = new Span("Double-click a row to edit");
        hint.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        return hint;
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

        String shortDescription = adjective + " " + noun;
        return restrictToLength(shortDescription, MAX_TEXT_IN_GRID);
    }

    public static String formatDescription(LocationData aLocationData) {
        return formatDescription(aLocationData.getDescriptionData());
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
            if (!command.isEmpty()) {
                command.append(" ");
            }
            command.append(adjective);
        }
        if (!noun.isEmpty()) {
            if (!command.isEmpty()) {
                command.append(" ");
            }
            command.append(noun);
        }
        return restrictToLength(command.toString(), MAX_TEXT_IN_GRID);
    }

    public static String formatDescription(Describable aDescribable) {
        String shortDescription = aDescribable.getShortDescription();
        if (shortDescription.isEmpty()) {
            shortDescription = getShortDescription(aDescribable.getNoun(), aDescribable.getAdjective());
        }
        return restrictToLength(shortDescription, MAX_TEXT_IN_GRID);
    }

    public static String formatDescription(DescriptionData aDescriptionData) {
        String shortDescription = aDescriptionData.getShortDescription();
        if (shortDescription.isEmpty()) {
            shortDescription = getWordText(aDescriptionData.getAdjective()) + " " +
                               getWordText(aDescriptionData.getNoun());
        }
        return restrictToLength(shortDescription, MAX_TEXT_IN_GRID);
    }

    private static String restrictToLength(String aText, int maxLength) {
        if (aText.length() <= maxLength) {
            return aText;
        }
        return aText.substring(0, maxLength) + "...";
    }

    public static void bindField(Binder<DirectionData> binder, ComboBox<String> field, VocabularyData aVocabulary,
                                 Word.Type type, CommandDescriptionData commandDescriptionData) {
        binder.forField(field).bind(_ -> {
            return getWordText(getWord(commandDescriptionData, type));
        }, (_, word) -> {
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
        Optional<Word> word = aVocabulary.findWord(aWordText);
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
        locationDataBinder.bind(aField, _ -> getWordText(getWord(aDescriptionData, aType)),
                                (_, word) -> {
                                    setWord(aVocabulary, word, aType, aDescriptionData);
                                });
    }

    private static void setWord(VocabularyData aVocabulary, String aWordText, Word.Type aType,
                                DescriptionData aDescriptionData) {
        Optional<Word> word = aVocabulary.findWord(aWordText);
        if (word.isEmpty()) {
            return;
        }
        Word foundWord = word.get();
        switch (aType) {
            case NOUN -> aDescriptionData.setNoun(foundWord);
            case ADJECTIVE -> aDescriptionData.setAdjective(foundWord);
            default -> throw new IllegalStateException("Unexpected value: " + aType);
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
            default -> throw new IllegalStateException("Unexpected value: " + aType);
        }
    }

    public static ConfirmDialog getConfirmDialog(final String aHeader, final String aType, final String anId) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(aHeader);

        dialog.setText("Are you sure you want to delete " + aType + " '" + anId + "'?");
        dialog.setConfirmButtonTheme("error primary");

        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        return dialog;
    }

    public static void showUsages(final String aHeader, String aType, String anItemId,
                                  final List<? extends TrackedUsage> usages) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(aHeader + ": " + anItemId);
        dialog.setWidth("700px");

        if (usages.isEmpty()) {
            dialog.setText("This " + aType + " is not currently used in any commands.");
        } else {
            StringBuilder usageText = new StringBuilder();
            usageText.append("This ").append(aType).append(" is referenced ").append(usages.size())
                     .append(" time(s):\n\n");

            for (TrackedUsage usage : usages) {
                usageText.append("• ").append(usage.getDisplayText()).append("\n");
            }

            Span usageSpan = new Span(usageText.toString());
            usageSpan.getStyle()
                     .set("white-space", "pre-wrap")
                     .set("font-family", "monospace")
                     .set("font-size", "0.9em");

            VerticalLayout content = new VerticalLayout(usageSpan);
            content.setPadding(false);
            dialog.setText(content);
        }

        dialog.setConfirmText("Close");
        dialog.open();
    }

    public static void setSize(Grid<?> aGrid) {
        aGrid.setSizeFull();
//        aGrid.setMaxWidth("1024px");
        aGrid.setMinWidth("640px");
        aGrid.setMaxHeight("640px");
        aGrid.setMinHeight("500px");
    }
}
