package com.pdg.adventure.server.parser;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.VocabularyData;

public class GenericCommandDescription implements CommandDescription {
    private String id;
    @Getter
    private final String verb;
    @Getter
    private final String adjective;
    @Getter
    private final String noun;
    // Not part of the command's identity (excluded from equals/hashCode below, and from the
    // COMMAND_TRIPLET-keyed matching in GenericCommandProvider/Workflow) - preposition/adverb/
    // adjective2/noun2 are ambient parse results that PrepositionCondition/AdverbCondition/
    // Adjective2Condition/Noun2Condition check via GameContext, not extra dimensions of which
    // command a "verb noun" spec refers to.
    @Getter
    private final String preposition;
    @Getter
    private final String adverb;
    // The second noun+adjective pair, e.g. "USE SPANNER ON ANCIENT MACHINE" - noun/adjective
    // stay "spanner"/"" (the primary command's match key); adjective2/noun2 hold "ancient"/
    // "machine" for Adjective2Condition/Noun2Condition to check.
    @Getter
    private final String adjective2;
    @Getter
    private final String noun2;

    public GenericCommandDescription(String aVerb, Describable aNamedThing) {
        this(aVerb, aNamedThing.getAdjective(), aNamedThing.getNoun());
    }

    public GenericCommandDescription(String aVerb) {
        this(aVerb, VocabularyData.EMPTY_STRING, VocabularyData.EMPTY_STRING);
    }

    public GenericCommandDescription(String aVerb, String aNoun) {
        this(aVerb, VocabularyData.EMPTY_STRING, aNoun);
    }

    public GenericCommandDescription(String aVerb, String anAdjective, String aNoun) {
        this(aVerb, anAdjective, aNoun, VocabularyData.EMPTY_STRING, VocabularyData.EMPTY_STRING);
    }

    public GenericCommandDescription(String aVerb, String anAdjective, String aNoun,
            String aPreposition, String anAdverb) {
        this(aVerb, anAdjective, aNoun, aPreposition, anAdverb,
                VocabularyData.EMPTY_STRING, VocabularyData.EMPTY_STRING);
    }

    public GenericCommandDescription(String aVerb, String anAdjective, String aNoun,
            String aPreposition, String anAdverb, String anAdjective2, String aNoun2) {
        verb = aVerb;
        adjective = anAdjective;
        noun = aNoun;
        preposition = aPreposition;
        adverb = anAdverb;
        adjective2 = anAdjective2;
        noun2 = aNoun2;
        id = UUID.randomUUID().toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
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
