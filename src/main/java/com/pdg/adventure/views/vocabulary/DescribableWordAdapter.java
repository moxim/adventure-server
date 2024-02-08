package com.pdg.adventure.views.vocabulary;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.Word;

public class DescribableWordAdapter implements Describable {
    private final Word word;

    public DescribableWordAdapter(Word aWord) {
        word = aWord;
    }

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

    public Word getWord() {
        return word;
    }

    public String getSynonym() {
        if (word == null || word.getSynonym() == null) {
            return "";
        }
        Word synonym = word.getSynonym();
        return synonym.getText();
    }
}
