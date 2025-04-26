package com.pdg.adventure.api;

public interface CommandDescription extends Comparable<CommandDescription>, Describable {
    String COMMAND_SEPARATOR = "|";
    String COMMAND_TRIPLET = "%s"+ COMMAND_SEPARATOR+"%s"+COMMAND_SEPARATOR+"%s";

    String getVerb();

    default String getDescription() {
        return String.format(COMMAND_TRIPLET, getVerb(), getAdjective(), getNoun());
    }

    default String getBasicDescription() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    default String getEnrichedBasicDescription() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    default String getShortDescription() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    default String getLongDescription() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    default String getEnrichedShortDescription() {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
