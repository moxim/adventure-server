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
}
