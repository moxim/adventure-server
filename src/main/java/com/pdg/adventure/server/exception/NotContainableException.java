package com.pdg.adventure.server.exception;

import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;

// TODO: this is not used anywhere; remove it.
class NotContainableException extends RuntimeException {
    public static final String CANNOT_PUT_TEXT = "You can't put the ";

    public NotContainableException(Containable anItem, Container aContainer) {
        super(CANNOT_PUT_TEXT + anItem + " into the " + aContainer + ".");
    }
}
