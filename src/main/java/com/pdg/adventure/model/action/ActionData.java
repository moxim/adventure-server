package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basic.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class ActionData extends BasicData {
    public String getActionName() {
        return this.getClass().getSimpleName();
    }

}
