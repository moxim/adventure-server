package com.pdg.adventure.view.item;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.view.support.ViewSupporter;

public class ItemLocationPairAdapter implements Describable {

    private final transient ItemLocationPair pair;

    public ItemLocationPairAdapter(ItemLocationPair aPair) {
        pair = aPair;
    }

    @Override
    public String getId() {
        return pair.item().getId();
    }

    @Override
    public void setId(String anId) {
        pair.item().setId(anId);
    }

    @Override
    public String getAdjective() {
        return pair.item().getDescriptionData().getSafeAdjective();
    }

    @Override
    public String getNoun() {
        return pair.item().getDescriptionData().getSafeNoun();
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
        return pair.item().getDescriptionData().getShortDescription();
    }

    @Override
    public String getLongDescription() {
        return null;
    }

    @Override
    public String getEnrichedShortDescription() {
        return null;
    }

    public String getLocationDescription() {
        return ViewSupporter.formatDescription(pair.location());
    }

    public String getContainable() {
        return pair.item().isContainable() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT;
    }

    public String getWearable() {
        return pair.item().isWearable() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT;
    }

    public String getWorn() {
        return pair.item().isWorn() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT;
    }

    public ItemLocationPair getPair() {
        return pair;
    }
}
