package com.pdg.adventure.api;

public interface Describable extends Ided {
    String getAdjective();

    String getNoun();

    String getBasicDescription();
    default String getStrippedBasicDescription() {
        return getBasicDescription();
    }
    String getEnrichedBasicDescription();

    String getShortDescription();
    default String getStrippedShortDescription() {
        return getShortDescription();
    }
    String getEnrichedShortDescription();

    String getLongDescription();
}
