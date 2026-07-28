package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Random;
import java.util.function.IntSupplier;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ChanceCondition extends AbstractCondition {

    @Getter
    private final Integer chance;
    private final transient IntSupplier randomSource;

    public ChanceCondition(Integer aChance) {
        this(aChance, () -> new Random().nextInt(100) + 1);
    }

    ChanceCondition(Integer aChance, IntSupplier aRandomSource) {
        chance = aChance;
        randomSource = aRandomSource;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        int roll = randomSource.getAsInt();
        if (roll <= chance.intValue()) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }

    @Override
    public boolean isDeterministic() {
        return false;
    }
}
