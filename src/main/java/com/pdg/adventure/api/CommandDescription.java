package com.pdg.adventure.api;

public interface CommandDescription extends Comparable<CommandDescription>, Describable {
    String COMMAND_SEPARATOR = "|";
    String COMMAND_TRIPLET = "%s" + COMMAND_SEPARATOR + "%s" + COMMAND_SEPARATOR + "%s";

    String getVerb();

    default String getDescription() {
        return COMMAND_TRIPLET.formatted(getVerb(), getAdjective(), getNoun());
    }

    default String getBasicDescription() {
        return getDescription();
    }

    default String getEnrichedBasicDescription() {
        return getDescription();
    }

    default String getShortDescription() {
        return getDescription();
    }

    default String getLongDescription() {
        return getDescription();
    }

    default String getEnrichedShortDescription() {
        return getDescription();
    }
}
