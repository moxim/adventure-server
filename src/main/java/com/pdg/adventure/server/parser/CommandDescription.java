package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.vocabulary.Vocabulary;

public class CommandDescription implements Comparable<CommandDescription> {
    private final String verb;
    private final String adjective;
    private final String noun;

    public CommandDescription (String aVerb, Describable aNamedThing) {
        this(aVerb, aNamedThing.getAdjective(), aNamedThing.getNoun());
    }

    public CommandDescription(String aVerb) {
        this(aVerb, Vocabulary.EMPTY_STRING, Vocabulary.EMPTY_STRING);
    }

    public CommandDescription(String aVerb, String aNoun) {
        this(aVerb, Vocabulary.EMPTY_STRING, aNoun);
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

    @Override
    public int compareTo(CommandDescription o) {
        return this.getDescription().compareTo(o.getDescription());
    }
}
