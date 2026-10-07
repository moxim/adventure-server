package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.GameSnapshotData;
import com.pdg.adventure.model.SavedGameData;
import com.pdg.adventure.server.storage.repository.SavedGameRepository;
import com.pdg.adventure.server.storage.service.SavedGameService;

/** Proves the Mongo mapping of a saved game and its nested snapshot, and the upsert-by-deterministic-id slot rule. */
@DataMongoTest
@Import(value = {de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
class SavedGamePersistenceTest {

    @BeforeEach
    void cleanDatabase(@Autowired MongoTemplate mongoTemplate) {
        mongoTemplate.getDb().drop();
    }

    private static GameSnapshotData snapshot() {
        GameSnapshotData snapshot = new GameSnapshotData();
        snapshot.setCurrentLocationId("01loc");
        snapshot.setCurrentPictureId("pic-1");
        snapshot.getKnownItemIds().addAll(List.of("01a", "01b", "01c"));
        snapshot.containerContents("01pocket").addAll(List.of("01b", "01a"));
        snapshot.containerContents("01hall");
        snapshot.getWornItemIds().add("01b");
        snapshot.getLumen().put("01a", 50);
        snapshot.getVisits().put("01loc", 3);
        snapshot.getVariables().put("score", 7);
        snapshot.getVariables().put("VISITED", 2);
        return snapshot;
    }

    @Test
    void aSavedGame_roundTripsWithItsSnapshot(@Autowired SavedGameRepository repository) {
        SavedGameService service = new SavedGameService(repository);
        Instant savedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        service.save("user-1", "adv-1", 3, "1.0.0", snapshot(), savedAt);

        SavedGameData loaded = service.find("user-1", "adv-1", 3).orElseThrow();
        assertThat(loaded.getId()).isEqualTo("user-1:adv-1:3");
        assertThat(loaded.getSavedAt()).isEqualTo(savedAt);
        assertThat(loaded.getBuilderVersion()).isEqualTo("1.0.0");
        assertThat(loaded.getSnapshot()).isEqualTo(snapshot());
        assertThat(loaded.getSnapshot().getContainers().get("01pocket")).containsExactly("01b", "01a");
        assertThat(loaded.getSnapshot().getContainers()).containsKey("01hall");
    }

    @Test
    void savingIntoTheSameSlotTwice_keepsOneDocument(@Autowired SavedGameRepository repository,
                                                     @Autowired MongoTemplate mongoTemplate) {
        SavedGameService service = new SavedGameService(repository);
        service.save("user-1", "adv-1", 2, "1.0.0", snapshot(), Instant.now());

        GameSnapshotData later = snapshot();
        later.getVariables().put("score", 99);
        service.save("user-1", "adv-1", 2, "1.0.1", later, Instant.now());

        assertThat(mongoTemplate.findAll(SavedGameData.class)).hasSize(1);
        SavedGameData loaded = service.find("user-1", "adv-1", 2).orElseThrow();
        assertThat(loaded.getBuilderVersion()).isEqualTo("1.0.1");
        assertThat(loaded.getSnapshot().getVariables()).containsEntry("score", 99);
    }

    @Test
    void listAndFreeSlot_onlySeeTheOwnersSavesOfThatAdventure(@Autowired SavedGameRepository repository) {
        SavedGameService service = new SavedGameService(repository);
        service.save("user-1", "adv-1", 1, null, snapshot(), Instant.now());
        service.save("user-1", "adv-1", 3, null, snapshot(), Instant.now());
        service.save("user-2", "adv-1", 2, null, snapshot(), Instant.now());
        service.save("user-1", "adv-2", 2, null, snapshot(), Instant.now());

        assertThat(service.list("user-1", "adv-1")).extracting(SavedGameData::getSlot).containsExactly(1, 3);
        assertThat(service.freeSlot("user-1", "adv-1")).hasValue(2);
        assertThat(service.freeSlot("user-2", "adv-1")).hasValue(1);
        assertThat(service.list("user-3", "adv-1")).isEmpty();
    }
}
