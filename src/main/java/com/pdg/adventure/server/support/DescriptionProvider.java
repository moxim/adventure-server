package com.pdg.adventure.server.support;

import javax.validation.constraints.NotNull;

public class DescriptionProvider {
    public static final String NOUN_MISSING_MESSAGE = "The noun is mandatory.";
    private final String noun;
    private String adjective;
    private String shortDescription;
    private String longDescription;

    public DescriptionProvider(String aNoun) {
        this(null, aNoun);
    }

    public DescriptionProvider(String anAdjective, @NotNull(message = NOUN_MISSING_MESSAGE) String aNoun) {
        validateParameters(aNoun);
        adjective = anAdjective;
        if (adjective == null) {
            adjective = Environment.EMPTY_STRING;
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
            if (Environment.EMPTY_STRING.equals(adjective)) {
                shortDescription = noun;
            } else {
                shortDescription = adjective + " " + noun;
            }
            shortDescription = ArticleProvider.prependUnknownArticle(shortDescription);
        }
        return shortDescription;
    }

    public void setShortDescription(String aShortDescription) {
        shortDescription = aShortDescription;
    }

    public String getLongDescription() {
        if (longDescription == null) {
            longDescription = "You see " + getShortDescription() + ".";
        }
        return longDescription;
    }

    public void setLongDescription(String aLongDescription) {
        longDescription = aLongDescription;
    }
}
