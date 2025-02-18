package com.pdg.adventure.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SameConditionData extends PreConditionData {
    private String variableNameOne;
    private String variableNameTwo;
}
