package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.pdg.adventure.model.basic.DatedData;
import com.pdg.adventure.model.basic.DescriptionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class ThingData extends DatedData {
    private DescriptionData descriptionData = new DescriptionData();
    private CommandProviderData commandProviderData = new CommandProviderData();
}
