package com.pdg.adventure.api;

public interface ExecutionResult {
    enum State {
        SUCCESS, FAILURE
    }

    State getExecutionState();

    String getResultMessage();

    void setExecutionState(State anExecutionState);

    void setResultMessage(String aResultMessage);

    boolean hasCommandMatched();

    void setCommandHasMatched();
}
