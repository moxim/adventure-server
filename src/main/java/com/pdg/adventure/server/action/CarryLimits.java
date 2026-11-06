package com.pdg.adventure.server.action;

import java.util.Optional;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.tangible.Item;

/**
 * The player's ability to carry things, as set by the ABILITY action: a maximum number of objects (the pocket's
 * max size) and a maximum weight. Used by the actions that take an item into the pocket.
 */
final class CarryLimits {

    private CarryLimits() {
    }

    /**
     * Why the player cannot take this item into the pocket, or empty if they can: the pocket is full (SM27) or the
     * item would make the carried weight exceed the limit (SM43). The number is checked first. An item that is
     * already in the pocket is never refused here - there is nothing to add.
     */
    static Optional<String> refusalFor(Container aPocket, Item anItem) {
        if (aPocket.contains(anItem)) {
            return Optional.empty();
        }
        if (aPocket.getSize() >= aPocket.getMaxSize()) {
            return Optional.of(SystemMessageKey.SM27.defaultText());
        }
        // long: the default limit is Integer.MAX_VALUE (unlimited)
        if ((long) aPocket.getCarriedWeight() + anItem.getWeight() > aPocket.getMaxWeight()) {
            return Optional.of(SystemMessageKey.SM43.defaultText().formatted(anItem.getStrippedBasicDescription()));
        }
        return Optional.empty();
    }
}
