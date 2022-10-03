package com.pdg.adventure.server.exceptions;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;

public class AlreadyPresentException extends RuntimeException {
    public static final String ALREADY_PRESENT_TEXT = " is already present in the ";

    public AlreadyPresentException(Containable anItem, Container aContainer) {
        super(anItem + ALREADY_PRESENT_TEXT + aContainer + ".");
    }
}
