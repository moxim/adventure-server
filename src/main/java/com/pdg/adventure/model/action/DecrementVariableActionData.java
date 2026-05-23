package com.pdg.adventure.model.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.pdg.adventure.model.basic.BasicData;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DecrementVariableActionData extends BasicData {
    private String name;
    private String value;
}
