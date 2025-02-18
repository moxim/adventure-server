package com.pdg.adventure.model.action;

import com.pdg.adventure.model.basics.BasicData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class ActionData extends BasicData {
    public String getActionName() {
        return this.getClass().getSimpleName();
    }

}
