package com.pdg.adventure.server.support;

import com.pdg.adventure.server.vocabulary.Vocabulary;

public class DescriptionProvider {
    public static final String NOUN_MISSING_MESSAGE = "The noun is mandatory.";
    private final String noun;
    private String adjective;
    private String shortDescription;
    private String longDescription;

    public DescriptionProvider(String aNoun) {
        this(null, aNoun);
    }

    public DescriptionProvider(String anAdjective, String aNoun) {
        validateParameters(aNoun);
        adjective = anAdjective;
        if (adjective == null) {
            adjective = Vocabulary.EMPTY_STRING;
        }
        noun = aNoun;
    }

    private void validateParameters(String aNoun) {
        if (aNoun == null) {
            throw new IllegalArgumentException(NOUN_MISSING_MESSAGE);
        }
    }

    public String getAdjective() {
        return adjective;
    }

    public String getNoun() {
        return noun;
    }

    public String getShortDescription() {
        if (shortDescription == null) {
            if (Vocabulary.EMPTY_STRING.equals(adjective)) {
                shortDescription = noun;
            } else {
                shortDescription = adjective + " " + noun;
            }
        }
        return shortDescription;
    }

    public String getEnrichedShortDescription() {
        return ArticleProvider.prependIndefiniteArticle(getShortDescription());
    }

    public void setShortDescription(String aShortDescription) {
        shortDescription = aShortDescription;
    }

    public String getLongDescription() {
        if (longDescription == null) {
            longDescription = getShortDescription();
        }
        return longDescription;
    }

    public void setLongDescription(String aLongDescription) {
        longDescription = aLongDescription;
    }

    public String getBasicDescription() {
        String result = "";
        if (!Vocabulary.EMPTY_STRING.equals(adjective)) {
            result += adjective + " ";
        }
        result += noun;
        return result;
    }

    public String getEnrichedBasicDescription() {
        return ArticleProvider.prependDefiniteArticle(getBasicDescription());
    }
}
