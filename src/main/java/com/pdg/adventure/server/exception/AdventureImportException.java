package com.pdg.adventure.server.exception;

/** An adventure file could not be imported. The message is written for the author and may be shown as is. */
public class AdventureImportException extends RuntimeException {
    public AdventureImportException(String aMessage) {
        super(aMessage);
    }

    public AdventureImportException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }
}
