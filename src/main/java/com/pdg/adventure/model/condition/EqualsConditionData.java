package com.pdg.adventure.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class EqualsConditionData extends PreConditionData {
    private final String variableName;
    private final String value;
}
