package com.pdg.adventure.view.direction;

import lombok.Getter;

import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.Word;

@Getter
public final class DirectionViewModel {
    // "read-only" attributes
    private final DirectionData data;
    private String adventureId;
    private String locationId;
    // these attributes can be edited
    private final String id;
    private String destinationId;
    private Word verb;
    private Word adjective;
    private Word noun;
    private String shortDescription;
    private String longDescription;

    public DirectionViewModel() {
        this(new DirectionData());
    }

    public DirectionViewModel(DirectionData aDirectionData) {
        data = aDirectionData;
        id = data.getId();
        destinationId = data.getDestinationId();
        verb = data.getCommandData().getCommandDescription().getVerb();
        adjective = data.getCommandData().getCommandDescription().getAdjective();
        noun = data.getCommandData().getCommandDescription().getNoun();
        shortDescription = data.getDescriptionData().getShortDescription();
        longDescription = data.getDescriptionData().getLongDescription();
    }

    public void setId(String anId) {
        data.setId(anId);
    }

    public void setLocationId(String aLocationId) {
        locationId = aLocationId;
    }

    public void setAdventureId(String anId) {
        adventureId = anId;
    }

    public void setDestinationId(String aDestinationId) {
        this.destinationId = aDestinationId;
        data.setDestinationId(aDestinationId);
    }

    public void setVerb(Word aVerb) {
        this.verb = aVerb;
        data.getCommandData().getCommandDescription().setVerb(aVerb);
    }

    public void setNoun(Word aNoun) {
        this.noun = aNoun;
        data.getCommandData().getCommandDescription().setNoun(aNoun);
    }

    public void setAdjective(Word anAdjective) {
        this.adjective = anAdjective;
        data.getCommandData().getCommandDescription().setAdjective(anAdjective);
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
