package com.pdg.adventure.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ContainerData extends ItemData {
    private List<ItemData> contents = new ArrayList<>();
    private int maxSize;
    private boolean holdsDirections;
}
