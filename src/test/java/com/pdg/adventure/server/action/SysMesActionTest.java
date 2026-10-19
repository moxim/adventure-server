package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

class SysMesActionTest {

    @Test
    void execute_showsTheSystemMessageText_withoutBreakingTheLine() {
        SysMesAction action = new SysMesAction(SystemMessageKey.SM15);

        ExecutionResult result = action.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("OK.");
        assertThat(action.continuesLine()).isTrue();
    }

    @Test
    void execute_usesTheAdventuresOwnWording() {
        SysMesAction action = new SysMesAction(SystemMessageKey.SM15);

        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of("15", "Righto."))) {
            assertThat(action.execute().getResultMessage()).isEqualTo("Righto.");
        }
        assertThat(action.execute().getResultMessage()).isEqualTo("OK.");
    }
}
