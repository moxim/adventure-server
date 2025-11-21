package com.pdg.adventure.view.vocabulary;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.Word;

public record DescribableWordAdapter(Word word) implements Describable {

    public String getType() {
        return word.getType().name();
    }

    @Override
    public String getAdjective() {
        return "a";
    }

    @Override
    public String getNoun() {
        return "n";
    }

    @Override
    public String getBasicDescription() {
        return null;
    }

    @Override
    public String getEnrichedBasicDescription() {
        return null;
    }

    @Override
    public String getShortDescription() {
        return word.getText();
    }

    @Override
    public String getLongDescription() {
        return null;
    }

    @Override
    public String getEnrichedShortDescription() {
        return null;
    }

    @Override
    public String getId() {
        return word.getId();
    }

    @Override
    public void setId(String anId) {
        word.setId(anId);
    }

    public String getSynonym() {
        if (word == null || word.getSynonym() == null) {
            return "";
        }
        Word synonym = word.getSynonym();
        return synonym.getText();
    }
}
