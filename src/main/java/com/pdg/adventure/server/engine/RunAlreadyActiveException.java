package com.pdg.adventure.server.engine;

/** A run was requested while this browser session already has an active one. */
public class RunAlreadyActiveException extends RuntimeException {

    public RunAlreadyActiveException() {
        super("You already have a game running in another tab.");
    }
}
