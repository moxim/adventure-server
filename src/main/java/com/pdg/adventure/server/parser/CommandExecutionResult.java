package com.pdg.adventure.server.parser;

import lombok.Setter;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;

public class CommandExecutionResult implements ExecutionResult {
    @Setter
    private State executionState;
    @Setter
    private String resultMessage;
    private boolean commandHasMatched;

    // TODO
    //  check where these c'tors are used, replace with c'tor (state, msg), e.g. c'tor(FAILURE, "I don't understand")
    public CommandExecutionResult() {
        this(State.FAILURE);
    }

    public CommandExecutionResult(State aSuccessState) {
        this(aSuccessState, VocabularyData.EMPTY_STRING);
    }

    public CommandExecutionResult(State aSuccessState, String aResultMessage) {
        executionState = aSuccessState;
        resultMessage = aResultMessage;
        commandHasMatched = false;
    }

    public void setCommandHasMatched() {
        commandHasMatched = true;
    }

    @Override
    public boolean hasCommandMatched() {
        return commandHasMatched;
    }

    @Override
    public State getExecutionState() {
        return executionState;
    }

    @Override
    public String getResultMessage() {
        return resultMessage;
    }

    @Override
    public String toString() {
        return executionState + " [" + resultMessage + "]";
    }
}
