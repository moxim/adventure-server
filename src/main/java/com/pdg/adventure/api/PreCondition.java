package com.pdg.adventure.api;

public interface PreCondition extends Ided {
    ExecutionResult check();

    String getName();
}
