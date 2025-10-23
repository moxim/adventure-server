package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basic.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class InventoryActionData extends BasicData {
    private String messageConsumerId; // TODO: this does not work
    private String containerProviderId; // TODO: this does not work
}
