package com.pdg.adventure.server.parser;

import com.pdg.adventure.server.api.CommandDescription;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.util.Objects;

public class GenericCommandDescription implements Comparable<CommandDescription>, CommandDescription {
    private final String verb;
    private final String adjective;
    private final String noun;

    public GenericCommandDescription(String aVerb, Describable aNamedThing) {
        this(aVerb, aNamedThing.getAdjective(), aNamedThing.getNoun());
    }

    public GenericCommandDescription(String aVerb) {
        this(aVerb, Vocabulary.EMPTY_STRING, Vocabulary.EMPTY_STRING);
    }

    public GenericCommandDescription(String aVerb, String aNoun) {
        this(aVerb, Vocabulary.EMPTY_STRING, aNoun);
    }

    public GenericCommandDescription(String aVerb, String anAdjective, String aNoun) {
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

    @Override
    public boolean equals(Object aO) {
        if (this == aO) return true;
        if (!(aO instanceof GenericCommandDescription that)) return false;

        if (!Objects.equals(verb, that.verb)) return false;
        if (!Objects.equals(adjective, that.adjective)) return false;
        return Objects.equals(noun, that.noun);
    }

    @Override
    public int hashCode() {
        int result = verb != null ? verb.hashCode() : 0;
        result = 31 * result + (adjective != null ? adjective.hashCode() : 0);
        result = 31 * result + (noun != null ? noun.hashCode() : 0);
        return result;
    }
}
