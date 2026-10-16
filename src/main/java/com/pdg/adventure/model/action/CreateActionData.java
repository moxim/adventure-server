package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CreateActionData extends ActionData {
    private String thingId;
    private String containerProviderId; // i.e. location.id or thing.id
}
