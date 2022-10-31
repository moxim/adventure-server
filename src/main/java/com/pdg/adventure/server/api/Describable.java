package com.pdg.adventure.server.api;

public interface Describable extends Actionable {
    String getAdjective();

    String getNoun();

    String getShortDescription();

    String getLongDescription();

    String getEnrichedShortDescription();
}
