package com.pdg.adventure.server.engine;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.exception.ItemNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class ItemIdentifier {
    private static final String ITEM_NOT_FOUND_TEXT = "Item '%s %s' not found";
    private static final String TOO_MANY_MATCHES_TEXT = "Too many matches for '%s %s'";

    private ItemIdentifier() {
        // don't instantiate me'
    }

    public static Containable findItem(Container aContainer, String anAdjective, String aNoun) {
        List<Containable> items = aContainer.getContents();
        List<Containable> foundItems = new ArrayList<>();
        for (Containable item : items) {
            if (item.getNoun().equals(aNoun)) {
                if (!anAdjective.equals("") && !item.getAdjective().equals(anAdjective)) {
                    continue;
                }
                foundItems.add(item);
            }
        }

        if (foundItems.size() > 1) {
            throw new ItemNotFoundException(String.format(TOO_MANY_MATCHES_TEXT, anAdjective, aNoun));
        }
        if (foundItems.isEmpty()) {
            throw new ItemNotFoundException(String.format(ITEM_NOT_FOUND_TEXT, anAdjective, aNoun));
        }

        return foundItems.get(0);
    }

}
