package com.pdg.adventure.api;

public interface Describable extends Ided {
    String getAdjective();

    String getNoun();

    String getBasicDescription();

    String getEnrichedBasicDescription();

    String getShortDescription();

    String getLongDescription();

    String getEnrichedShortDescription();
}
