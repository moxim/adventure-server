package com.pdg.adventure.server.exceptions;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.tangible.Item;

public class NotContainableException extends RuntimeException {
    public static final String CANNOT_PUT_TEXT = "You can't put the ";

    public NotContainableException(Item anItem, Container aContainer) {
        super(CANNOT_PUT_TEXT + anItem + " into the " + aContainer + ".");
    }
}
