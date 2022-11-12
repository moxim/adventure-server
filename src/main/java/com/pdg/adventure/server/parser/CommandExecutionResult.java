package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.ExecutionResult;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class CommandExecutionResult implements ExecutionResult {
    private State executionState;
    private String resultMessage;

    List<ExecutionResult> otherResults;

    // TODO
    //  check where these c'tors are used, replace with c'tor (state, msg), e.g. c'tor(FAILURE, "I don't understand")
    public CommandExecutionResult() {
        this(State.FAILURE);
    }

    public CommandExecutionResult(State aSuccessState) {
        executionState = aSuccessState;
        resultMessage = Vocabulary.EMPTY_STRING;
        otherResults = new ArrayList<>();
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
}
