package com.pdg.adventure.model;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basics.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AdventureData extends BasicData {
    private ContainerData playerPocket = new ContainerData();
    private List<LocationData> locationDataList = new LinkedList<>();
    private BasicData currentLocationId = new BasicData();
    // WorkflowData workFlow;
}
