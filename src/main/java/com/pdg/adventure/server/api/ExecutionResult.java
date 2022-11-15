package com.pdg.adventure.server.api;

public interface ExecutionResult {
    enum State {
        SUCCESS, FAILURE
    }

    State getExecutionState();

    String getResultMessage();

    void setExecutionState(State anExecutionState);

    void setResultMessage(String aResultMessage);

    void add(ExecutionResult anExecutionResult);

    boolean hasCommandMatched();

    void setCommandHasMatched();
}
