package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class CommandExecutionResult implements ExecutionResult {
    private State executionState;
    private String resultMessage;
    private boolean commandHasMatched;
    private List<ExecutionResult> otherResults;

    // TODO
    //  check where these c'tors are used, replace with c'tor (state, msg), e.g. c'tor(FAILURE, "I don't understand")
    public CommandExecutionResult() {
        this(State.FAILURE);
    }

    public CommandExecutionResult(State aSuccessState) {
        executionState = aSuccessState;
        resultMessage = Vocabulary.EMPTY_STRING;
        otherResults = new ArrayList<>();
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
    public void add(ExecutionResult anExecutionResult) {
        otherResults.add(anExecutionResult);
    }

    public List<ExecutionResult> getOtherResults() {
        return otherResults;
    }

    @Override
    public String toString() {
        return executionState + " [" + resultMessage + "]";
    }
}
