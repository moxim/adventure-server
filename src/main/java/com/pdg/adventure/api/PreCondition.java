package com.pdg.adventure.api;

public interface PreCondition extends Ided {
    ExecutionResult check();

    String getName();

    // false for conditions whose check() has a side effect or isn't idempotent (e.g. rolling
    // a random chance) - callers that need to dry-run "would this currently pass" without
    // triggering that effect (or getting a different answer than the real check() will give a
    // moment later) must skip conditions where this is false rather than call check().
    default boolean isDeterministic() {
        return true;
    }

    /**
     * Called once on each precondition of a command whose preconditions all passed, just before its actions run
     * (whether they then succeed or not). A condition that must know its command applies (not merely that it itself
     * held) reacts here; the default does nothing. A wrapping condition such as NOT does not pass the call on, so what it wraps never reacts.
     */
    default void commandApplied() {
    }
}
