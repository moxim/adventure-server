package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basics.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class IncrementVariableActionData extends BasicData {
    private final String name;
    private final String value;
}
