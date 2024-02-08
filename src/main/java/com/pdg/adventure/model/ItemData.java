package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
//@ToString(callSuper = true)
public class ItemData extends ThingData {
    private boolean isContainable;
    @DBRef(db="itemContainerData", lazy = true)
    private UUID parentContainerId;// = new ItemContainerData();
    private boolean isWearable;
    private boolean isWorn;

    @Override
    public String toString() {
        return "ItemData{" +
                "id=" + getId() +
                "isContainable=" + isContainable +
                ", isWearable=" + isWearable +
                ", isWorn=" + isWorn +
                ", parentContainer=" + parentContainerId +
                '}';
    }
}
