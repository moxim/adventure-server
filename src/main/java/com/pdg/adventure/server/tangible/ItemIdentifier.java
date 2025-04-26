package com.pdg.adventure.server.tangible;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.VocabularyData;

public class ItemIdentifier {

    private static final Logger LOG = LoggerFactory.getLogger(ItemIdentifier.class);

    private ItemIdentifier() {
        // don't instantiate me
    }

    public static List<Containable> findItems(Container aContainer, CommandDescription aCommandDescription) {
        String noun = aCommandDescription.getNoun();
        String adjective = aCommandDescription.getAdjective();

        List<Containable> items = aContainer.getContents();
        List<Containable> foundItems = new ArrayList<>();

        if (VocabularyData.EMPTY_STRING.equals(noun)) { // TODO: when does this ever happen?
            LOG.warn("Drats! Empty noun in command description: {}", aCommandDescription);
            String verb = aCommandDescription.getVerb();
            foundItems.addAll(addItemsByVerb(items, verb));
        } else {
            foundItems.addAll(addItemsByName(items, noun, adjective));
        }

        return foundItems;
    }

    private static <T extends Containable> List<Containable> addItemsByVerb(List<T> items, String aVerb) {
        final List<Containable> matchingItems = new ArrayList<>();
        for (T item : items) {
            if (item.hasVerb(aVerb)) {
                matchingItems.add(item);
            }
        }
        return matchingItems;
    }

    private static <T extends Containable> List<Containable> addItemsByName(List<T> aListOfItems, String aNoun, String anAdjective) {
        final List<Containable> matchingItems = new ArrayList<>();
        if (!VocabularyData.EMPTY_STRING.equals(aNoun)) {
            for (T item : aListOfItems) {
                if (item.getNoun().equals(aNoun) && item.getAdjective().equals(anAdjective)) {
                    matchingItems.add(item);
                }
            }
            if (matchingItems.isEmpty()) {
                for (T item : aListOfItems) {
                    if (item.getNoun().equals(aNoun)) {
                        matchingItems.add(item);
                    }
                }
            }
        }
        return matchingItems;
    }

}
