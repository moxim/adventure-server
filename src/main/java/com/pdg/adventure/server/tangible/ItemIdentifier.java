package com.pdg.adventure.server.tangible;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class ItemIdentifier {

    private ItemIdentifier() {
        // don't instantiate me
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
