package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basic.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CreateActionData extends BasicData {
    private String thingId;
    private String containerProviderId; // i.e. location.id or thing.id
}
