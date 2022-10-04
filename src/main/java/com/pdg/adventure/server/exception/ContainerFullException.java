package com.pdg.adventure.server.exception;

import com.pdg.adventure.server.api.Container;

public class ContainerFullException extends RuntimeException {
    public static final String ALREADY_FULL_TEXT = " is already full.";

    public ContainerFullException(Container aContainer) {
        super("The " + aContainer + ALREADY_FULL_TEXT);
    }
}
