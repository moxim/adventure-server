package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.support.Environment;

public class CommandDescription {
    private final String verb;
    private final String adjective;
    private final String noun;

    public CommandDescription (String aVerb, Describable aNamedThing) {
        this(aVerb, aNamedThing.getAdjective(), aNamedThing.getNoun());
    }

    public CommandDescription(String aVerb) {
        this(aVerb, Environment.EMPTY_STRING, Environment.EMPTY_STRING);
    }

    public CommandDescription(String aVerb, String aNoun) {
        this(aVerb, Environment.EMPTY_STRING, aNoun);
    }

    public CommandDescription(String aVerb, String anAdjective, String aNoun) {
        verb = aVerb;
        adjective = anAdjective;
        noun = aNoun;
    }

    public String getVerb() {
        return verb;
    }

    public String getAdjective() {
        return adjective;
    }

    public String getNoun() {
        return noun;
    }

    public String getDescription() {
        return verb + "_" + adjective + "_" + noun;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
