package com.pdg.adventure.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AndConditionData extends PreConditionData {
    private PreConditionData preCondition;
    private PreConditionData anotherPreCondition;
}
