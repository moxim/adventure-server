package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class InventoryActionData extends ActionData {
    private String messageConsumerId; // TODO: this does not work
    private String containerProviderId; // TODO: this does not work
}
