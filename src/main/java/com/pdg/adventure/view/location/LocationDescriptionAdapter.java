package com.pdg.adventure.view.location;

import lombok.Getter;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.view.support.ViewSupporter;

public class LocationDescriptionAdapter implements Describable {
    private final LocationData locationData;
    @Getter
    private final int usageCount;

    public LocationDescriptionAdapter(LocationData aLocationData, int aUsageCount) {
        locationData = aLocationData;
        usageCount = aUsageCount;
    }

    @Override
    public String getAdjective() {
        return ViewSupporter.getWordText(locationData.getDescriptionData().getAdjective());
    }

    @Override
    public String getNoun() {
        return ViewSupporter.getWordText(locationData.getDescriptionData().getNoun());
    }

    @Override
    public String getBasicDescription() {
        return ViewSupporter.getWordText(locationData.getDescriptionData().getAdjective()) + " " +
               ViewSupporter.getWordText(locationData.getDescriptionData().getNoun());
    }

    @Override
    public String getEnrichedBasicDescription() {
        return getBasicDescription();
    }

    @Override
    public String getShortDescription() {
        return locationData.getDescriptionData().getShortDescription();
    }

    @Override
    public String getLongDescription() {
        return locationData.getDescriptionData().getLongDescription();
    }

    @Override
    public String getEnrichedShortDescription() {
        return locationData.getDescriptionData().getShortDescription();
    }

    @Override
    public String getId() {
        return locationData.getId();
    }

    @Override
    public void setId(String anId) {
        locationData.setId(anId);
    }

    public int getLumen() {
        return locationData.getLumen();
    }
}
