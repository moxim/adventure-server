package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
//@ToString(callSuper = true)
public class ItemData extends ThingData {
    private boolean isContainable;
    @DBRef(lazy = true)
    private ItemContainerData parentContainer;
    private boolean isWearable;
    private boolean isWorn;

    @Override
    public String toString() {
        return "ItemData{" +
                "isContainable=" + isContainable +
                ", isWearable=" + isWearable +
                ", isWorn=" + isWorn +
                ", parentContainer=" + parentContainer +
                '}';
    }
}
