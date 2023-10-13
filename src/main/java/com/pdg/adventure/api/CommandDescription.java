package com.pdg.adventure.api;

public interface CommandDescription extends Comparable<CommandDescription>, Describable {
    String getVerb();

    default String getDescription() {
        return String.format("%s_%s_%s", getVerb(), getAdjective(), getNoun());
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
