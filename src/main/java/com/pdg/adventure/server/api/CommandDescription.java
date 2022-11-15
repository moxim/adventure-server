package com.pdg.adventure.server.api;

public interface CommandDescription extends Comparable<CommandDescription> {
    String getVerb();

    String getAdjective();

    String getNoun();

    default String getDescription() {
        return String.format("%s_%s_%s", getVerb(), getAdjective(), getNoun());
    }
}
