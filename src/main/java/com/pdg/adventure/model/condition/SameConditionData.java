package com.pdg.adventure.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basics.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SameConditionData extends BasicData {
    private String variableNameOne;
    private String variableNameTwo;
}
