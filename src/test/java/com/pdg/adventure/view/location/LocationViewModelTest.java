package com.pdg.adventure.view.location;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.DescriptionData;

class LocationViewModelTest {

    private LocationData createLocationData() {
        LocationData data = new LocationData();
        data.setId("loc-1");
        data.setLumen(75);
        DescriptionData desc = new DescriptionData();
        Word noun = new Word("forest", Word.Type.NOUN);
        Word adjective = new Word("dark", Word.Type.ADJECTIVE);
        desc.setNoun(noun);
        desc.setAdjective(adjective);
        desc.setShortDescription("The dark forest");
        desc.setLongDescription("A dark and mysterious forest.");
        data.setDescriptionData(desc);
        return data;
    }

    @Test
    void constructor_populatesAllFieldsFromLocationData() {
        LocationData data = createLocationData();
        LocationViewModel vm = new LocationViewModel(data);

        assertThat(vm.getId()).isEqualTo("loc-1");
        assertThat(vm.getLumen()).isEqualTo(75);
        assertThat(vm.getNoun().getText()).isEqualTo("forest");
        assertThat(vm.getAdjective().getText()).isEqualTo("dark");
        assertThat(vm.getShortDescription()).isEqualTo("The dark forest");
        assertThat(vm.getLongDescription()).isEqualTo("A dark and mysterious forest.");
        assertThat(vm.getData()).isSameAs(data);
    }

    @Test
    void getNumberOfExits_returnsCountFromDirectionsData() {
        LocationData data = createLocationData();
        Set<DirectionData> directions = new HashSet<>();
        directions.add(new DirectionData());
        directions.add(new DirectionData());
        data.setDirectionsData(directions);

        LocationViewModel vm = new LocationViewModel(data);

        assertThat(vm.getNumberOfExits()).isEqualTo(2);
    }

    @Test
    void setNoun_updatesViewModelAndUnderlyingData() {
        LocationData data = createLocationData();
        LocationViewModel vm = new LocationViewModel(data);
        Word newNoun = new Word("cave", Word.Type.NOUN);

        vm.setNoun(newNoun);

        assertThat(vm.getNoun()).isSameAs(newNoun);
        assertThat(data.getDescriptionData().getNoun()).isSameAs(newNoun);
    }

    @Test
    void setAdjective_updatesViewModelAndUnderlyingData() {
        LocationData data = createLocationData();
        LocationViewModel vm = new LocationViewModel(data);
        Word newAdj = new Word("bright", Word.Type.ADJECTIVE);

        vm.setAdjective(newAdj);

        assertThat(vm.getAdjective()).isSameAs(newAdj);
        assertThat(data.getDescriptionData().getAdjective()).isSameAs(newAdj);
    }

    @Test
    void setShortDescription_updatesViewModelAndUnderlyingData() {
        LocationData data = createLocationData();
        LocationViewModel vm = new LocationViewModel(data);

        vm.setShortDescription("A bright cave");

        assertThat(vm.getShortDescription()).isEqualTo("A bright cave");
        assertThat(data.getDescriptionData().getShortDescription()).isEqualTo("A bright cave");
    }

    @Test
    void setLongDescription_updatesViewModelAndUnderlyingData() {
        LocationData data = createLocationData();
        LocationViewModel vm = new LocationViewModel(data);

        vm.setLongDescription("A long detailed description.");

        assertThat(vm.getLongDescription()).isEqualTo("A long detailed description.");
        assertThat(data.getDescriptionData().getLongDescription()).isEqualTo("A long detailed description.");
    }

    @Test
    void setLumen_updatesViewModelAndUnderlyingData() {
        LocationData data = createLocationData();
        LocationViewModel vm = new LocationViewModel(data);

        vm.setLumen(25);

        assertThat(vm.getLumen()).isEqualTo(25);
        assertThat(data.getLumen()).isEqualTo(25);
    }

    @Test
    void setId_updatesViewModelAndUnderlyingData() {
        LocationData data = createLocationData();
        LocationViewModel vm = new LocationViewModel(data);

        vm.setId("new-loc-id");

        assertThat(vm.getId()).isEqualTo("new-loc-id");
        assertThat(data.getId()).isEqualTo("new-loc-id");
    }

    @Test
    void setAdventureId_updatesAdventureId() {
        LocationViewModel vm = new LocationViewModel(createLocationData());

        vm.setAdventureId("adv-123");

        assertThat(vm.getAdventureId()).isEqualTo("adv-123");
    }

    @Test
    void numberOfExits_isZero_whenNoDirections() {
        LocationViewModel vm = new LocationViewModel(createLocationData());
        assertThat(vm.getNumberOfExits()).isZero();
    }
}
