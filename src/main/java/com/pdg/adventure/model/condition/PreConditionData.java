package com.pdg.adventure.model.condition;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.basic.BasicData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PreConditionData extends BasicData implements PreCondition {
    @Override
    public ExecutionResult check() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
