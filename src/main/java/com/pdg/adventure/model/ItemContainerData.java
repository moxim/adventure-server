package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ItemContainerData extends ItemData {
    @DBRef(lazy = true)
//    @CascadeSave
    private List<ItemData> items = new ArrayList<>();
    private int maxSize;
    private boolean holdingDirections;

    @Override
    public String toString() {
        return "ItemContainerData{" +
                "id=" + getId() +
                "}";
    }
}
