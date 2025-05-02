package com.pdg.adventure.server.support;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.basics.DescriptionData;

public class DescriptionProvider implements Ided {
    public static final String NOUN_MISSING_MESSAGE = "The noun is mandatory.";
    private String id;
    @Getter
    private final String noun;
    @Getter
    @Setter
    private String adjective;
    @Setter
    private String shortDescription;
    @Setter
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

    public String getShortDescription() {
        if (shortDescription == null) {
            shortDescription = getBasicDescription();
        }
        return shortDescription;
    }

    public String getEnrichedShortDescription(String aDescription) {
        return ArticleProvider.prependIndefiniteArticle(aDescription);
    }

    public String getLongDescription() {
        if (longDescription == null) {
            longDescription = getShortDescription();
        }
        return longDescription;
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
