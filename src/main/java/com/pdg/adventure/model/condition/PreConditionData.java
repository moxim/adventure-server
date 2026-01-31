package com.pdg.adventure.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basic.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PreConditionData extends BasicData {
    public String getPreconditionName() {
        return this.getClass().getSimpleName();
    }

}
