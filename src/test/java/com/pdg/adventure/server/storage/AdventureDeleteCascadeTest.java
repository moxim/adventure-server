package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.SystemMessageData;
import com.pdg.adventure.model.basic.BasicData;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;
import com.pdg.adventure.server.storage.repository.AdventureRepository;
import com.pdg.adventure.server.storage.repository.LocationRepository;
import com.pdg.adventure.server.storage.service.AdventureService;

/**
 * Deleting an adventure must remove every document that belongs to it: locations, their
 * containers and items, and the player's pocket. Messages and system messages are embedded
 * directly in the adventure document (not {@code @DBRef}), so they round-trip and disappear
 * along with it automatically - this test also proves that round-trip actually works.
 * <p>
 * The test mirrors {@link com.pdg.adventure.server.storage.service.AdventureService#deleteAdventure}
 * exactly: the adventure is RELOADED from MongoDB before deletion, so the remaining lazy
 * {@code @DBRef} field (the player pocket) is a lazy-loading proxy — just as in production.
 */
@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 CascadeDeleteHelper.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
class AdventureDeleteCascadeTest {

    @BeforeEach
    void cleanDatabase(@Autowired MongoTemplate mongoTemplate) {
        mongoTemplate.getDb().drop();
    }

    @Test
    void deletingAnAdventureRemovesAllItsDocuments(@Autowired MongoTemplate mongoTemplate,
                                                   @Autowired CascadeDeleteHelper cascadeDeleteHelper) {
        // given: an adventure with a location (holding an item), an item in the player's pocket
        // and a message — saved through the regular cascade-save path
        AdventureData adventure = new AdventureData();
        adventure.setTitle("cascade delete test");

        LocationData location = new LocationData();
        ItemData locationItem = new ItemData();
        location.getItemContainerData().getItems().add(locationItem);
        adventure.getLocationData().put(location.getId(), location);

        ItemData pocketItem = new ItemData();
        adventure.getPlayerPocket().getItems().add(pocketItem);

        MessageData message = new MessageData(adventure.getId(), "greeting", "hello");
        adventure.getMessages().put(message.getMessageId(), message);

        SystemMessageData override = new SystemMessageData(adventure.getId(), "0", "It's pitch black.");
        adventure.getSystemMessages().put(override.getKey(), override);

        mongoTemplate.save(adventure);

        assertSoftly(softly -> {
            softly.assertThat(mongoTemplate.findAll(ItemContainerData.class))
                  .as("containers after save (location container + pocket)").hasSize(2);
            softly.assertThat(mongoTemplate.findAll(ItemData.class))
                  .as("items after save").hasSize(2);
        });

        // when: deleting the same way AdventureService.deleteAdventure does — load fresh, cascade, remove
        AdventureData reloaded = mongoTemplate.findById(adventure.getId(), AdventureData.class);
        cascadeDeleteHelper.cascadeDelete(reloaded);

        // the embedded messages/systemMessages round-trip as plain fields of the adventure
        // document — no lazy DBRef proxy, no risk of a dangling-reference crash on access
        assertSoftly(softly -> {
            softly.assertThat(reloaded.getMessages().get("greeting")).extracting(MessageData::getText)
                  .as("embedded message survives a save/reload round trip").isEqualTo("hello");
            softly.assertThat(reloaded.getSystemMessages().get("0")).extracting(SystemMessageData::getText)
                  .as("embedded system message override survives a save/reload round trip")
                  .isEqualTo("It's pitch black.");
        });

        mongoTemplate.remove(reloaded);

        // then: nothing of the adventure may survive
        assertSoftly(softly -> {
            softly.assertThat(mongoTemplate.findAll(AdventureData.class))
                  .as("adventures").isEmpty();
            softly.assertThat(mongoTemplate.findAll(LocationData.class))
                  .as("locations").isEmpty();
            softly.assertThat(mongoTemplate.findAll(ItemContainerData.class))
                  .as("containers").isEmpty();
            softly.assertThat(mongoTemplate.findAll(ItemData.class))
                  .as("items").isEmpty();
        });
    }

    @Test
    void deletingALocationRemovesItsContainerAndItems(@Autowired MongoTemplate mongoTemplate,
                                                      @Autowired CascadeDeleteHelper cascadeDeleteHelper,
                                                      @Autowired LocationRepository locationRepository,
                                                      @Autowired AdventureRepository adventureRepository) {
        // given: an adventure with two locations, each holding one item
        AdventureData adventure = new AdventureData();
        adventure.setTitle("location delete test");

        LocationData keptLocation = new LocationData();
        ItemData keptItem = new ItemData();
        keptLocation.getItemContainerData().getItems().add(keptItem);
        adventure.getLocationData().put(keptLocation.getId(), keptLocation);

        LocationData doomedLocation = new LocationData();
        ItemData doomedItem = new ItemData();
        doomedLocation.getItemContainerData().getItems().add(doomedItem);
        adventure.getLocationData().put(doomedLocation.getId(), doomedLocation);

        mongoTemplate.save(adventure);
        assertThat(mongoTemplate.findAll(ItemContainerData.class))
                .as("containers after save (pocket + 2 location containers)").hasSize(3);

        // when: deleting one location the way LocationsMenuView does — remove it from the
        // adventure's map, delete the location, save the adventure
        AdventureService adventureService = new AdventureService(locationRepository, adventureRepository,
                                                                 null, null, cascadeDeleteHelper);
        adventure.getLocationData().remove(doomedLocation.getId());
        adventureService.deleteLocation(doomedLocation.getId());
        adventureService.saveAdventureData(adventure);

        // then: the deleted location's container and item are gone, everything else survives
        assertSoftly(softly -> {
            softly.assertThat(mongoTemplate.findAll(LocationData.class)).extracting(BasicData::getId)
                  .as("locations").containsExactly(keptLocation.getId());
            softly.assertThat(mongoTemplate.findAll(ItemContainerData.class)).extracting(BasicData::getId)
                  .as("only the kept location's container and the pocket may remain")
                  .containsExactlyInAnyOrder(keptLocation.getItemContainerData().getId(),
                                             adventure.getPlayerPocket().getId());
            softly.assertThat(mongoTemplate.findAll(ItemData.class)).extracting(BasicData::getId)
                  .as("items").containsExactly(keptItem.getId());
        });
    }
}
