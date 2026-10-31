package com.pdg.adventure.view.direction;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.basic.DescriptionData;

/**
 * Builds a {@link DirectionData} the way the {@link DirectionEditorView} saves one, so that every view that creates
 * directions (the editor and the world map) produces the same data.
 */
public final class DirectionBuilder {

    private DirectionBuilder() {
    }

    /**
     * Gives the direction the adjective and noun of its owning location, makes moving the player to the direction's
     * destination its only action and adds it to the owner. Does not save. Safe to call again after the destination
     * changed.
     */
    public static void attach(DirectionData aDirection, LocationData anOwner) {
        final DescriptionData ownerDescription = anOwner.getDescriptionData();
        final DescriptionData directionDescription = aDirection.getDescriptionData();
        directionDescription.setAdjective(ownerDescription.getAdjective());
        directionDescription.setNoun(ownerDescription.getNoun());

        final MovePlayerActionData movePlayerAction = new MovePlayerActionData();
        movePlayerAction.setLocationId(aDirection.getDestinationId());
        aDirection.getCommandData().setActions(new ArrayList<>(List.of(movePlayerAction)));

        anOwner.getDirectionsData().add(aDirection);
    }

    /** A new direction of the owner to the destination, followed with the given words; the short and long descriptions stay empty. */
    public static DirectionData create(LocationData anOwner, String aDestinationId, Word aVerb, Word anAdjective,
                                       Word aNoun) {
        final DirectionData direction = new DirectionData();
        direction.setDestinationId(aDestinationId);
        direction.getCommandData().getCommandDescription().setVerb(aVerb);
        direction.getCommandData().getCommandDescription().setAdjective(anAdjective);
        direction.getCommandData().getCommandDescription().setNoun(aNoun);
        attach(direction, anOwner);
        return direction;
    }
}
