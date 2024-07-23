package com.pdg.adventure.server.tangible;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.VocabularyData;

import java.util.ArrayList;
import java.util.List;

public class ItemIdentifier {

    private ItemIdentifier() {
        // don't instantiate me
    }

    public static List<Containable> findItems(Container aContainer, CommandDescription aCommandDescription) {
        String noun = aCommandDescription.getNoun();
        String adjective = aCommandDescription.getAdjective();

        List<Containable> foundItems = new ArrayList<>();
        List<Containable> items = aContainer.getContents();
        addItemByName(items, foundItems, noun, adjective);

        if (foundItems.isEmpty() && VocabularyData.EMPTY_STRING.equals(noun)) {
            String verb = aCommandDescription.getVerb();
            addItemByVerbAlone(items, foundItems, verb);
        }
        return foundItems;
    }

    private static <T extends Containable> void addItemByVerbAlone(List<T> items, List<T> aFoundItems, String aVerb) {
        for (T item : items) {
            if (item.hasVerb(aVerb)) {
                aFoundItems.add(item);
            }
        }
    }

    private static <T extends Containable> void addItemByName(List<T> items, List<T> foundItems, String noun, String adjective) {
        if (!VocabularyData.EMPTY_STRING.equals(noun)) {
            for (T item : items) {
                if (item.getNoun().equals(noun)) {
                    if (!(VocabularyData.EMPTY_STRING.equals(adjective) || item.getAdjective().equals(adjective))) {
                        continue;
                    }
                    foundItems.add(item);
                }
            }
        }
    }

}
