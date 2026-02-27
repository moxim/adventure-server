package com.pdg.adventure.view.direction;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.view.support.ViewSupporter;

public class DirectionDescriptionAdapter implements Describable {

    private final DirectionData directionData;
    private final AdventureData adventureData;

    public DirectionDescriptionAdapter(DirectionData aDirectionData, AdventureData anAdventureData) {
        directionData = aDirectionData;
        adventureData = anAdventureData;
    }

    @Override
    public String getId() {
        return directionData.getId();
    }

    @Override
    public void setId(String anId) {
        directionData.setId(anId);
    }

    @Override
    public String getAdjective() {
        return ViewSupporter.getWordText(directionData.getCommandData().getCommandDescription().getAdjective());
    }

    @Override
    public String getNoun() {
        return ViewSupporter.getWordText(directionData.getCommandData().getCommandDescription().getNoun());
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
        return ViewSupporter.formatDescription(directionData.getCommandData().getCommandDescription());
    }

    @Override
    public String getLongDescription() {
        return null;
    }

    @Override
    public String getEnrichedShortDescription() {
        return null;
    }

    public String getDestinationDescription() {
        String destinationId = directionData.getDestinationId();
        if (destinationId == null || destinationId.isEmpty()) {
            return "";
        }
        LocationData destination = adventureData.getLocationData().get(destinationId);
        if (destination == null) {
            return "";
        }
        return ViewSupporter.formatDescription(destination);
    }

    public String getDestinationId() {
        String destinationId = directionData.getDestinationId();
        if (destinationId == null || destinationId.isEmpty()) {
            return "";
        }
        return ViewSupporter.formatId(destinationId);
    }

    public DirectionData getDirectionData() {
        return directionData;
    }
}
