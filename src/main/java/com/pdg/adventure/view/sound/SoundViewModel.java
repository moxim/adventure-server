package com.pdg.adventure.view.sound;

import lombok.Getter;

import com.pdg.adventure.model.SoundData;

/** Binds the editor's fields to a {@link SoundData}; setters write through, as for pictures. */
@Getter
public final class SoundViewModel {
    private final SoundData data;

    private String id;
    private String name;
    private String adventureId;

    public SoundViewModel(SoundData aSoundData) {
        data = aSoundData;
        id = data.getId();
        name = data.getName();
        adventureId = data.getAdventureId();
    }

    public void setId(String anId) {
        id = anId;
        data.setId(anId);
    }

    /** The name is kept trimmed: names are unique ignoring surrounding spaces. */
    public void setName(String aName) {
        name = aName == null ? null : aName.trim();
        data.setName(name);
    }

    public void setAdventureId(String anAdventureId) {
        adventureId = anAdventureId;
        data.setAdventureId(anAdventureId);
    }
}
