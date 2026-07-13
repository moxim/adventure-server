package com.pdg.adventure.view.location;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.DescriptionData;

class LocationDescriptionAdapterTest {

    private LocationData createLocationData() {
        LocationData data = new LocationData();
        data.setId("loc-1");
        data.setLumen(60);
        DescriptionData desc = new DescriptionData();
        Word noun = new Word("lake", Word.Type.NOUN);
        Word adjective = new Word("misty", Word.Type.ADJECTIVE);
        desc.setNoun(noun);
        desc.setAdjective(adjective);
        desc.setShortDescription("The misty lake");
        desc.setLongDescription("A beautiful misty lake.");
        data.setDescriptionData(desc);
        return data;
    }

    @Test
    void getNoun_returnsNounText() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getNoun()).isEqualTo("lake");
    }

    @Test
    void getAdjective_returnsAdjectiveText() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getAdjective()).isEqualTo("misty");
    }

    @Test
    void getId_returnsLocationId() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getId()).isEqualTo("loc-1");
    }

    @Test
    void setId_updatesLocationId() {
        LocationData data = createLocationData();
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(data, 0);

        adapter.setId("new-id");

        assertThat(adapter.getId()).isEqualTo("new-id");
        assertThat(data.getId()).isEqualTo("new-id");
    }

    @Test
    void getShortDescription_returnsShortDescription() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getShortDescription()).isEqualTo("The misty lake");
    }

    @Test
    void getLongDescription_returnsLongDescription() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getLongDescription()).isEqualTo("A beautiful misty lake.");
    }

    @Test
    void getBasicDescription_combinesAdjectiveAndNoun() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getBasicDescription()).isEqualTo("misty lake");
    }

    @Test
    void getEnrichedBasicDescription_returnsBasicDescription() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getEnrichedBasicDescription()).isEqualTo(adapter.getBasicDescription());
    }

    @Test
    void getEnrichedShortDescription_returnsShortDescription() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getEnrichedShortDescription()).isEqualTo("The misty lake");
    }

    @Test
    void getLumen_returnsLocationLumen() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 0);
        assertThat(adapter.getLumen()).isEqualTo(60);
    }

    @Test
    void getUsageCount_returnsProvidedCount() {
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(createLocationData(), 5);
        assertThat(adapter.getUsageCount()).isEqualTo(5);
    }

    @Test
    void getNoun_withNullWord_returnsEmptyString() {
        LocationData data = createLocationData();
        data.getDescriptionData().setNoun(null);
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(data, 0);

        assertThat(adapter.getNoun()).isEmpty();
    }

    @Test
    void getAdjective_withNullWord_returnsEmptyString() {
        LocationData data = createLocationData();
        data.getDescriptionData().setAdjective(null);
        LocationDescriptionAdapter adapter = new LocationDescriptionAdapter(data, 0);

        assertThat(adapter.getAdjective()).isEmpty();
    }
}
