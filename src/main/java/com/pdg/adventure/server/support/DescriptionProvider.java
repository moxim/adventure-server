package com.pdg.adventure.server.support;

import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.basics.DescriptionData;

import java.util.UUID;

public class DescriptionProvider implements Ided {
    public static final String NOUN_MISSING_MESSAGE = "The noun is mandatory.";
    private String id;
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
            adjective = VocabularyData.EMPTY_STRING;
        }
        noun = aNoun;
        id = UUID.randomUUID().toString();
    }

    public DescriptionProvider(DescriptionData descriptionData) {
        this(descriptionData.getAdjective().getText(), descriptionData.getNoun().getText());
        setShortDescription(descriptionData.getShortDescription());
        setLongDescription(descriptionData.getLongDescription());
    }

    private void validateParameters(String aNoun) {
        if (aNoun == null) {
            throw new IllegalArgumentException(NOUN_MISSING_MESSAGE);
        }
    }

    public String getAdjective() {
        return adjective;
    }

    public void setAdjective(String anAdjective) {
        adjective = anAdjective;
    }

    public String getNoun() {
        return noun;
    }

    public String getShortDescription() {
        if (shortDescription == null) {
            shortDescription = getBasicDescription();
        }
        return shortDescription;
    }

    public String getEnrichedShortDescription(String aDescription) {
        return ArticleProvider.prependIndefiniteArticle(aDescription);
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
        if (!VocabularyData.EMPTY_STRING.equals(adjective)) {
            result += adjective + " ";
        }
        result += noun;
        return result;
    }

    public String getEnrichedBasicDescription() {
        return ArticleProvider.prependDefiniteArticle(getBasicDescription());
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
        return "DescriptionProvider{" +
                "id='" + id + '\'' +
                ", noun='" + noun + '\'' +
                ", adjective='" + adjective + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", longDescription='" + longDescription + '\'' +
                '}';
    }
}
