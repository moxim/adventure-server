package com.pdg.adventure.server.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String aMessage) {
        super(aMessage);
    }
}
