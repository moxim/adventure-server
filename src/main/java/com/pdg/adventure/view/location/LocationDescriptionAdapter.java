package com.pdg.adventure.view.location;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.view.support.ViewSupporter;

public class LocationDescriptionAdapter implements Describable {
    private final LocationData locationData;
    private int usageCount;

    public LocationDescriptionAdapter(LocationData aLocationData) {
        this.locationData = aLocationData;
        this.usageCount = 0;
    }

    public LocationDescriptionAdapter(LocationData aLocationData, int aUsageCount) {
        this.locationData = aLocationData;
        this.usageCount = aUsageCount;
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
        return ViewSupporter.getWordText(locationData.getDescriptionData().getAdjective()) + " " + ViewSupporter.getWordText(locationData.getDescriptionData().getNoun());
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

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int aUsageCount) {
        this.usageCount = aUsageCount;
    }
}
