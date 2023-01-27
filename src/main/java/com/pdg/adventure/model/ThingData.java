package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.pdg.adventure.model.basics.BasicData;
import com.pdg.adventure.model.basics.DescriptionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ThingData extends BasicData {
    private DescriptionData descriptionData;
    private CommandProviderData commandProviderData;
}
