package com.pdg.adventure.server.exception;

public class AmbiguousCommandException extends RuntimeException {
    public AmbiguousCommandException(String aMessage) {
        super(aMessage);
    }
}
