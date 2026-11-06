package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.server.storage.mongo.CascadeDelete;

class AdventureDataSoundTest {

    @Test
    void newAdventure_hasEmptySoundData() {
        assertThat(new AdventureData().getSoundData()).isNotNull().isEmpty();
    }

    @Test
    void soundData_isAnEagerCascadeDeletedReference() throws NoSuchFieldException {
        Field field = AdventureData.class.getDeclaredField("soundData");

        assertThat(field.isAnnotationPresent(CascadeDelete.class)).isTrue();
        assertThat(field.getAnnotation(DBRef.class).lazy()).isFalse();
    }

    @Test
    void soundContent_isALazyCascadeDeletedReference_soAnAdventureRunNeverLoadsAudioBytes()
            throws NoSuchFieldException {
        Field field = SoundData.class.getDeclaredField("content");

        assertThat(field.isAnnotationPresent(CascadeDelete.class)).isTrue();
        assertThat(field.getAnnotation(DBRef.class).lazy()).isTrue();
    }
}
