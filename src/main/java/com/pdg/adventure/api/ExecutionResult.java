package com.pdg.adventure.api;

import java.io.Serializable;

public interface ExecutionResult extends Serializable {
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
