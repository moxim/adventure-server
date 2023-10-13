package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ItemContainerData extends ItemData {
    private List<ItemData> contents = new ArrayList<>();
    private int maxSize;
    private boolean holdingDirections;
}
