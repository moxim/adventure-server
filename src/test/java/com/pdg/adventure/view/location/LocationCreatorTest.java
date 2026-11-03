package com.pdg.adventure.view.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;

class LocationCreatorTest {

    private final Word cellar = new Word("cellar", Word.Type.NOUN);
    private final Word dark = new Word("dark", Word.Type.ADJECTIVE);

    private AdventureData adventure;

    @BeforeEach
    void setUp() {
        adventure = new AdventureData();
    }

    @Test
    void createAddsAnUnplacedLocationWithTheGivenWordsAndDescriptionToTheAdventure() {
        LocationData created = LocationCreator.create(adventure, cellar, dark, "  a damp cellar ");

        assertThat(adventure.getLocationData()).containsEntry(created.getId(), created);
        assertThat(created.getDescriptionData().getNoun()).isEqualTo(cellar);
        assertThat(created.getDescriptionData().getAdjective()).isEqualTo(dark);
        assertThat(created.getDescriptionData().getShortDescription()).isEqualTo("a damp cellar");
        assertThat(created.getMapColumn()).isNull();
        assertThat(created.getMapRow()).isNull();
    }

    @Test
    void theItemsInTheLocationAreDescribedByItsNounAndAdjective() {
        LocationData created = LocationCreator.create(adventure, cellar, dark, null);

        assertThat(created.getItemContainerData().getDescriptionData().getNoun()).isEqualTo(cellar);
        assertThat(created.getItemContainerData().getDescriptionData().getAdjective()).isEqualTo(dark);
    }

    @Test
    void adjectiveAndShortDescriptionAreOptional() {
        LocationData created = LocationCreator.create(adventure, cellar, null, "   ");

        assertThat(created.getDescriptionData().getAdjective()).isNull();
        assertThat(created.getDescriptionData().getShortDescription()).isNotEqualTo("   ");
    }

    @Test
    void everyCreatedLocationGetsItsOwnId() {
        LocationData first = LocationCreator.create(adventure, cellar, null, null);
        LocationData second = LocationCreator.create(adventure, cellar, null, null);

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(adventure.getLocationData()).hasSize(2);
    }
}
