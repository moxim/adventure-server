package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationDataTest {

    @Test
    void newLocationIsUnplaced() {
        LocationData location = new LocationData();

        assertThat(location.getMapColumn()).isNull();
        assertThat(location.getMapRow()).isNull();
    }
}
