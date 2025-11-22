package com.pdg.adventure.view.item;

import lombok.Getter;

import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.Word;

@Getter
public final class ItemViewModel {
    // "read-only" attributes
    private final ItemData data;
    private final String adventureId;

    // these attributes can be edited
    private String id;
    private Word noun;
    private Word adjective;
    private String shortDescription;
    private String longDescription;
    private boolean isContainable;
    private boolean isWearable;
    private boolean isWorn;
    private final String locationId;

    public ItemViewModel(ItemData anItemData) {
        data = anItemData;
        adventureId = data.getAdventureId();
        locationId = data.getLocationId();
        id = data.getId();
        noun = data.getDescriptionData().getNoun();
        adjective = data.getDescriptionData().getAdjective();
        shortDescription = data.getDescriptionData().getShortDescription();
        longDescription = data.getDescriptionData().getLongDescription();
        isContainable = data.isContainable();
        isWearable = data.isWearable();
        isWorn = data.isWorn();
    }

    public void setId(String anId) {
        id = anId;
        data.setId(anId);
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

    public void setContainable(boolean isContainable) {
        this.isContainable = isContainable;
        data.setContainable(isContainable);
    }

    public void setWearable(boolean isWearable) {
        this.isWearable = isWearable;
        data.setWearable(isWearable);
    }

    public void setWorn(boolean isWorn) {
        this.isWorn = isWorn;
        data.setWorn(isWorn);
    }

    public String getLocationId() {
        return locationId;
    }
}
