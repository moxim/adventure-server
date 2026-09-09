package com.pdg.adventure.server.exception;

import com.pdg.adventure.api.Container;

// TODO: this is not used anywhere; remove it.
class ContainerFullException extends RuntimeException {
    public static final String ALREADY_FULL_TEXT = " is already full.";

    public ContainerFullException(Container aContainer) {
        super("The " + aContainer + ALREADY_FULL_TEXT);
    }
}
