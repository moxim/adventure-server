package com.pdg.adventure.model.basics;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DescriptionData extends BasicDescriptionData {
    private String shortDescription = "";
    private String longDescription = "";

    public DescriptionData() {
    }

    public DescriptionData(String aShortDescription, String aLongDescription) {
        this.shortDescription = aShortDescription;
        this.longDescription = aLongDescription;
    }
}
