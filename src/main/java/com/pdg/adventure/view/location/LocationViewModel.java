package com.pdg.adventure.view.location;

import lombok.Getter;
import lombok.Setter;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;

@Getter
public final class LocationViewModel {
    // "read-only" attributes
    private final LocationData data;
    private final int numberOfExits;
    @Setter
    private String adventureId;

    // these attributes can be edited
    private String id;
    private int lumen;
    private Word noun;
    private Word adjective;
    private String shortDescription;
    private String longDescription;

    public LocationViewModel(LocationData aLocationData) {
        data = aLocationData;
        numberOfExits = data.getDirectionsData().size();
        id = data.getId();
        lumen = data.getLumen();
        noun = data.getDescriptionData().getNoun();
        adjective = data.getDescriptionData().getAdjective();
        shortDescription = data.getDescriptionData().getShortDescription();
        longDescription = data.getDescriptionData().getLongDescription();
    }

    public void setId(String anId) {
        id = anId;
        data.setId(anId);
    }

    public void setLumen(Integer aValue) {
        lumen = aValue;
        data.setLumen(aValue);
    }

    public void setNoun(Word aNoun) {
        this.noun = aNoun;
        data.getDescriptionData().setNoun(aNoun);
    }

    public void setAdjective(Word anAdjective) {
        this.adjective = anAdjective;
        data.getDescriptionData().setAdjective(anAdjective);
    }

    public void setLongDescription(String aDescription) {
        this.longDescription = aDescription;
        data.getDescriptionData().setLongDescription(aDescription);
    }

    public void setShortDescription(String aDescription) {
        this.shortDescription = aDescription;
        data.getDescriptionData().setShortDescription(aDescription);
    }
}
