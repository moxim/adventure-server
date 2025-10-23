package com.pdg.adventure.server.support;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import com.pdg.adventure.api.Ided;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.DescriptionData;

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

    public DescriptionProvider(String noun) {
        this(null, noun);
    }

    public DescriptionProvider(String adjective, String noun) {
//        validateParameters(noun);
        this.adjective = adjective != null ? adjective : VocabularyData.EMPTY_STRING;
        this.noun = noun;
        this.id = UUID.randomUUID().toString();
    }

    public DescriptionProvider(Word adjective, Word noun) {
//        validateParameters(noun);
        this.adjective = adjective != null && adjective.getText() != null ? adjective.getText() : VocabularyData.EMPTY_STRING;
        this.noun = noun.getText();
        this.id = UUID.randomUUID().toString();
    }

    public DescriptionProvider(DescriptionData descriptionData) {
        this(
            descriptionData.getAdjective() != null ? descriptionData.getAdjective().getText() : null,
            descriptionData.getNoun() != null ? descriptionData.getNoun().getText() : ""
        );
        setShortDescription(descriptionData.getShortDescription() != null ? descriptionData.getShortDescription() : "");
        setLongDescription(descriptionData.getLongDescription() != null ? descriptionData.getLongDescription() : "");
    }

    private void validateParameters(String noun) {
        if (noun == null || noun.trim().isEmpty()) {
            throw new IllegalArgumentException(NOUN_MISSING_MESSAGE);
        }
    }

    private void validateParameters(Word noun) {
        if (noun == null || noun.getText() == null || noun.getText().trim().isEmpty()) {
            throw new IllegalArgumentException(NOUN_MISSING_MESSAGE);
        }
    }

    public String getShortDescription() {
        if (shortDescription == null || shortDescription.isEmpty()) {
            shortDescription = getBasicDescription();
        }
        return shortDescription;
    }

    public String getEnrichedShortDescription(String description) {
        return ArticleProvider.prependIndefiniteArticle(description != null && !description.isEmpty() ? description : getBasicDescription());
    }

    public String getLongDescription() {
        if (longDescription == null || longDescription.isEmpty()) {
            longDescription = getShortDescription();
        }
        return longDescription;
    }

    public String getBasicDescription() {
        StringBuilder result = new StringBuilder();
        if (adjective != null && !adjective.isEmpty()) {
            result.append(adjective).append(" ");
        }
        result.append(noun);
        return result.toString();
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
