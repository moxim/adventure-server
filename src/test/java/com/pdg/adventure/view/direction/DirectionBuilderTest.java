package com.pdg.adventure.view.direction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.action.MovePlayerActionData;

class DirectionBuilderTest {

    private final Word verb = new Word("go", Word.Type.VERB);
    private final Word adjective = new Word("dark", Word.Type.ADJECTIVE);
    private final Word noun = new Word("north", Word.Type.NOUN);

    private LocationData ownerWithNoun(Word aNoun) {
        LocationData owner = new LocationData();
        owner.getDescriptionData().setNoun(aNoun);
        return owner;
    }

    @Test
    void createSetsWordsDestinationAndMoveAction() {
        LocationData owner = ownerWithNoun(new Word("cave", Word.Type.NOUN));

        DirectionData direction = DirectionBuilder.create(owner, "dest", verb, adjective, noun);

        assertThat(direction.getCommandData().getCommandDescription().getVerb()).isEqualTo(verb);
        assertThat(direction.getCommandData().getCommandDescription().getAdjective()).isEqualTo(adjective);
        assertThat(direction.getCommandData().getCommandDescription().getNoun()).isEqualTo(noun);
        assertThat(direction.getDestinationId()).isEqualTo("dest");
        assertThat(direction.getCommandData().getActions()).hasSize(1)
                                                         .first().isInstanceOf(MovePlayerActionData.class);
        assertThat(((MovePlayerActionData) direction.getCommandData().getActions().getFirst()).getLocationId())
                .isEqualTo("dest");
    }

    @Test
    void createAddsDirectionToOwnerAndCopiesOwnerWordsIntoDescription() {
        Word ownerNoun = new Word("cave", Word.Type.NOUN);
        LocationData owner = ownerWithNoun(ownerNoun);

        DirectionData direction = DirectionBuilder.create(owner, "dest", verb, null, noun);

        assertThat(owner.getDirectionsData()).contains(direction);
        assertThat(direction.getDescriptionData().getNoun()).isEqualTo(ownerNoun);
    }

    @Test
    void attachAgainAfterRepointingUpdatesMoveAction() {
        LocationData owner = ownerWithNoun(new Word("cave", Word.Type.NOUN));
        DirectionData direction = DirectionBuilder.create(owner, "dest", verb, null, noun);

        direction.setDestinationId("other");
        DirectionBuilder.attach(direction, owner);

        assertThat(direction.getCommandData().getActions()).hasSize(1);
        assertThat(((MovePlayerActionData) direction.getCommandData().getActions().getFirst()).getLocationId())
                .isEqualTo("other");
        assertThat(owner.getDirectionsData()).hasSize(1);
    }
}
