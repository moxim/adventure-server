package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.api.CommandDescription;
import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.exception.AmbiguousCommandException;
import com.pdg.adventure.server.exception.ItemNotFoundException;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class ItemIdentifier {
    private static final String ITEM_NOT_FOUND_TEXT = "Item '%s %s' not found";
    private static final String TOO_MANY_MATCHES_TEXT = "Too many matches for '%s %s'";

    private ItemIdentifier() {
        // don't instantiate me
    }

    public static Containable findItem(Container aContainer, CommandDescription aCommandDescription) {

        List<Containable> foundItems = findItems(aContainer, aCommandDescription);
        if (foundItems.size() == 1) {
            return foundItems.get(0);
        }

        String noun = aCommandDescription.getNoun();
        String adjective = aCommandDescription.getAdjective();

        if (foundItems.size() > 1) {
            throw new AmbiguousCommandException(String.format(TOO_MANY_MATCHES_TEXT, adjective, noun));
        }

        // no items found
        throw new ItemNotFoundException(String.format(ITEM_NOT_FOUND_TEXT, adjective, noun));
    }

    public static List<Containable> findItems(Container aContainer, CommandDescription aCommandDescription) {
        String noun = aCommandDescription.getNoun();
        String adjective = aCommandDescription.getAdjective();

        List<Containable> items = aContainer.getContents();
        List<Containable> foundItems = new ArrayList<>();
        addItemByName(items, foundItems, noun, adjective);

        if (foundItems.isEmpty() && Vocabulary.EMPTY_STRING.equals(noun)) {
            String verb = aCommandDescription.getVerb();
            addItemByVerbAlone(items, foundItems, verb);
        }
        return foundItems;
    }

    private static void addItemByVerbAlone(List<Containable> items, List<Containable> aFoundItems, String aVerb) {
         for (Containable item : items) {
            if (item.hasVerb(aVerb)) {
                aFoundItems.add(item);
            }
        }
    }

    private static void addItemByName(List<Containable> items, List<Containable> foundItems, String noun, String adjective) {
        if (!Vocabulary.EMPTY_STRING.equals(noun)) {
               for (Containable item : items) {
                if (item.getNoun().equals(noun)) {
                    if (!(Vocabulary.EMPTY_STRING.equals(adjective) || item.getAdjective().equals(adjective))) {
                        continue;
                    }
                    foundItems.add(item);
                }
            }
        }
    }

}
