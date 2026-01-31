package com.pdg.adventure.server.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.server.parser.CommandExecutionResult;

class NotConditionTest {

    @Test
    void check_shouldInvertSuccessToFailure() {
        // Given: a wrapped condition that returns SUCCESS
        PreCondition wrappedCondition = mock(PreCondition.class);
        ExecutionResult successResult = new CommandExecutionResult();
        successResult.setExecutionState(ExecutionResult.State.SUCCESS);
        successResult.setResultMessage("Original success message");
        when(wrappedCondition.check()).thenReturn(successResult);

        NotCondition sut = new NotCondition(wrappedCondition);

        // When: check is called
        ExecutionResult result = sut.check();

        // Then: result should be FAILURE with empty message
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEmpty();
    }

    @Test
    void check_shouldInvertFailureToSuccess() {
        // Given: a wrapped condition that returns FAILURE
        PreCondition wrappedCondition = mock(PreCondition.class);
        ExecutionResult failureResult = new CommandExecutionResult();
        failureResult.setExecutionState(ExecutionResult.State.FAILURE);
        failureResult.setResultMessage("Original failure message");
        when(wrappedCondition.check()).thenReturn(failureResult);

        NotCondition sut = new NotCondition(wrappedCondition);

        // When: check is called
        ExecutionResult result = sut.check();

        // Then: result should be SUCCESS with empty message
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEmpty();
    }

    @Test
    void check_shouldHandleMultipleStates() {
        PreCondition wrappedCondition = mock(PreCondition.class);

        assertAll(
            () -> {
                // when wrapped condition returns SUCCESS
                ExecutionResult successResult = new CommandExecutionResult();
                successResult.setExecutionState(ExecutionResult.State.SUCCESS);
                successResult.setResultMessage("Success message");
                when(wrappedCondition.check()).thenReturn(successResult);

                NotCondition sut = new NotCondition(wrappedCondition);
                ExecutionResult result = sut.check();

                // then result should be inverted to FAILURE
                assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
                assertThat(result.getResultMessage()).isEmpty();
            },
            () -> {
                // when wrapped condition returns FAILURE
                ExecutionResult failureResult = new CommandExecutionResult();
                failureResult.setExecutionState(ExecutionResult.State.FAILURE);
                failureResult.setResultMessage("Failure message");
                when(wrappedCondition.check()).thenReturn(failureResult);

                NotCondition sut = new NotCondition(wrappedCondition);
                ExecutionResult result = sut.check();

                // then result should be inverted to SUCCESS
                assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
                assertThat(result.getResultMessage()).isEmpty();
            }
        );
    }

    @Test
    void getWrappedCondition_shouldReturnTheWrappedCondition() {
        // Given: a NotCondition with a wrapped condition
        PreCondition wrappedCondition = mock(PreCondition.class);
        NotCondition sut = new NotCondition(wrappedCondition);

        // When: getting the wrapped condition
        PreCondition result = sut.getWrappedCondition();

        // Then: it should return the same wrapped condition
        assertThat(result).isEqualTo(wrappedCondition);
    }

    @Test
    void check_shouldClearResultMessageRegardlessOfState() {
        // Given: a wrapped condition that returns a result with a message
        PreCondition wrappedCondition = mock(PreCondition.class);
        ExecutionResult resultWithMessage = new CommandExecutionResult();
        resultWithMessage.setExecutionState(ExecutionResult.State.SUCCESS);
        resultWithMessage.setResultMessage("This message should be cleared");
        when(wrappedCondition.check()).thenReturn(resultWithMessage);

        NotCondition sut = new NotCondition(wrappedCondition);

        // When: check is called
        ExecutionResult result = sut.check();

        // Then: result message should be empty
        assertThat(result.getResultMessage()).isEmpty();
    }
}
