package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.pdg.adventure.model.basics.BasicData;
import com.pdg.adventure.model.basics.DescriptionData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class ThingData extends BasicData {
    private DescriptionData descriptionData = new DescriptionData();
    private CommandProviderData commandProviderData = new CommandProviderData();
}
