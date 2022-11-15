package com.pdg.adventure.server.exception;

public class QuitException extends RuntimeException {
    public QuitException() {
        super("Bye bye.");
    }
}
