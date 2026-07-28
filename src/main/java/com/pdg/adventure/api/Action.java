package com.pdg.adventure.api;

import java.io.Serializable;

public interface Action extends Serializable {
    ExecutionResult execute();

    String getActionName();

    // true for actions that only report state (e.g. printing a message) rather than changing
    // it - used to tell apart a command that "does something" from one that merely explains
    // why it couldn't, when disambiguating between multiple textually-matching commands.
    default boolean isInformationalOnly() {
        return false;
    }

    // true only for BreakAction - marks a command whose execution must stop the enclosing
    // command chain from processing any further commands (but not other chains, or actions
    // that ran before this one within the same command).
    default boolean isBreak() {
        return false;
    }
}
