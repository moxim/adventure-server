package com.pdg.adventure.views.locations;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;

import java.io.Serializable;

public final class LocationViewModel implements Serializable {
    private final LocationData data;
    private int defaultExits;
    private int lumen;
    private String id;
    private String adventureId;
    private Word noun;
    private Word adjective;
    private String longDescription;
    private String shortDescription;

    public LocationViewModel() {
        this(new LocationData());
    }

    public LocationViewModel(LocationData aLocationData) {
        data = aLocationData;
        defaultExits = data.getDirectionsData().size();
        lumen = data.getLumen();
        id = data.getId();
        adventureId = data.getAdventure().getId();
        noun = data.getDescriptionData().getNoun();
        adjective = data.getDescriptionData().getAdjective();
        longDescription = data.getDescriptionData().getLongDescription();
        shortDescription = data.getDescriptionData().getShortDescription();
    }

    public Integer getDefaultExits() {
        return data.getDirectionsData().size();
    }

    public LocationData getData() {
        return data;
    }

    public Integer getLumen() {
        return data.getLumen();
    }
    public void setLumen(Integer aValue) {
        data.setLumen(aValue);
    }

    public String getId() {
        return data.getId();
    }
    public void setId(String anId) {
        data.setId(anId);
    }

    public String getAdventureId() {
        return data.getAdventure().getId();
    }
    public void setAdventureId(String anId) {
        data.getAdventure().setId(anId);
    }

    public Word getNoun() {
        return data.getDescriptionData().getNoun();
    }
    public void setNoun(Word aNoun) {
        data.getDescriptionData().setNoun(aNoun);
    }

    public Word getAdjective() {
        return data.getDescriptionData().getAdjective();
    }
    public void setAdjective(Word anAdjective) {
        data.getDescriptionData().setAdjective(anAdjective);
    }

    public String getLongDescription() {
        return data.getDescriptionData().getLongDescription();
    }
    public void setLongDescription(String aDescription) {
        data.getDescriptionData().setLongDescription(aDescription);
    }

    public String getShortDescription() {
        return data.getDescriptionData().getShortDescription();
    }
    public void setShortDescription(String aDescription) {
        data.getDescriptionData().setShortDescription(aDescription);
    }
}
