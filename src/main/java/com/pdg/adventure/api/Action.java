package com.pdg.adventure.api;

public interface Action {
    ExecutionResult execute();

    String getActionName();
}
