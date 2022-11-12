package com.pdg.adventure.server.api;

public interface Action {
    ExecutionResult execute();

    String getName();
}
