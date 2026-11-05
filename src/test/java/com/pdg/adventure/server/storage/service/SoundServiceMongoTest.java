package com.pdg.adventure.server.storage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.LazyLoadingProxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SoundContentData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;
import com.pdg.adventure.server.storage.repository.SoundContentRepository;
import com.pdg.adventure.server.storage.repository.SoundRepository;

/**
 * SoundService against a real (embedded) MongoDB. The editor works on a sound the adventure loaded from the database,
 * whose content is a lazy proxy - unlike the mocks of SoundServiceTest, only this shows what saving does to it.
 */
@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 CascadeDeleteHelper.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
class SoundServiceMongoTest {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private SoundRepository soundRepository;
    @Autowired
    private SoundContentRepository contentRepository;
    @Autowired
    private CascadeDeleteHelper cascadeDeleteHelper;

    private SoundService service;
    private AdventureData adventure;
    private SoundData savedSound;

    @BeforeEach
    void saveAnAdventureWithOneSound() {
        mongoTemplate.getDb().drop();
        service = new SoundService(soundRepository, contentRepository, cascadeDeleteHelper);
        savedSound = new SoundData();
        savedSound.setName("pirate_bay");
        service.save(savedSound, new byte[] {1, 2, 3});
        adventure = new AdventureData();
        adventure.getSoundData().put(savedSound.getId(), savedSound);
        mongoTemplate.save(adventure);
    }

    private SoundData reloadedSound() {
        // exactly what the editor holds: loaded with the adventure, its content still a lazy proxy
        return mongoTemplate.findById(adventure.getId(), AdventureData.class).getSoundData().get(savedSound.getId());
    }

    @Test
    void replacingTheFileOfASavedSound_updatesTheSameContentDocument() {
        SoundData loaded = reloadedSound();
        assertThat(loaded.getContent()).as("the editor holds a lazy proxy, not the real object")
                                       .isInstanceOf(LazyLoadingProxy.class);

        service.save(loaded, new byte[] {9, 9});

        SoundData afterwards = reloadedSound();
        assertSoftly(softly -> {
            softly.assertThat(mongoTemplate.findAll(SoundContentData.class)).as("content documents").hasSize(1);
            softly.assertThat(afterwards.audioBytes()).as("new bytes").containsExactly(9, 9);
            softly.assertThat(afterwards.getSizeBytes()).isEqualTo(2);
            softly.assertThat(afterwards.getContent().getId()).as("same content document")
                  .isEqualTo(savedSound.getContent().getId());
        });
    }

    @Test
    void renamingASavedSoundWithoutANewFile_keepsItsContent() {
        SoundData loaded = reloadedSound();
        loaded.setName("renamed");

        service.save(loaded, null);

        SoundData afterwards = reloadedSound();
        assertSoftly(softly -> {
            softly.assertThat(afterwards.getName()).isEqualTo("renamed");
            softly.assertThat(afterwards.audioBytes()).containsExactly(1, 2, 3);
            softly.assertThat(mongoTemplate.findAll(SoundContentData.class)).hasSize(1);
        });
    }
}
