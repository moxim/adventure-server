package com.pdg.adventure.view.picture;

import lombok.Getter;

import com.pdg.adventure.model.PictureData;

@Getter
public final class PictureViewModel {
    private final PictureData data;

    private String id;
    private String name;
    private String adventureId;

    public PictureViewModel(PictureData aPictureData) {
        data = aPictureData;
        id = data.getId();
        name = data.getName();
        adventureId = data.getAdventureId();
    }

    public void setId(String anId) {
        id = anId;
        data.setId(anId);
    }

    public void setName(String aName) {
        name = aName;
        data.setName(aName);
    }

    public void setAdventureId(String anAdventureId) {
        adventureId = anAdventureId;
        data.setAdventureId(anAdventureId);
    }
}
