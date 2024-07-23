package com.pdg.adventure.server.parser;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;

public class CommandExecutionResult implements ExecutionResult {
    private State executionState;
    private String resultMessage;
    private boolean commandHasMatched;

    // TODO
    //  check where these c'tors are used, replace with c'tor (state, msg), e.g. c'tor(FAILURE, "I don't understand")
    public CommandExecutionResult() {
        this(State.FAILURE);
    }

    public CommandExecutionResult(State aSuccessState) {
        executionState = aSuccessState;
        resultMessage = VocabularyData.EMPTY_STRING;
        commandHasMatched = false;
    }

    public void setCommandHasMatched() {
        commandHasMatched = true;
    }

    @Override
    public boolean hasCommandMatched() {
        return commandHasMatched;
    }

    public void setExecutionState(State anExecutionState) {
        this.executionState = anExecutionState;
    }

    public void setResultMessage(String aResultMessage) {
        this.resultMessage = aResultMessage;
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
