package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class MessageDataTest {

    @Test
    void constructor_default_shouldInitializeFieldsWithDefaults() {
        // When
        MessageData messageData = new MessageData();

        // Then
        assertThat(messageData.getCreatedDate()).isNotNull();
        assertThat(messageData.getModifiedDate()).isNotNull();
        assertThat(messageData.getTranslations()).isNotNull().isEmpty();
        assertThat(messageData.getTags()).isNotNull().isEmpty();
    }

    @Test
    void constructor_withParameters_shouldSetFieldsCorrectly() {
        // Given
        String adventureId = "adventure-123";
        String messageId = "welcome_msg";
        String text = "Welcome to the adventure!";

        // When
        MessageData messageData = new MessageData(adventureId, messageId, text);

        // Then
        assertThat(messageData.getAdventureId()).isEqualTo(adventureId);
        assertThat(messageData.getMessageId()).isEqualTo(messageId);
        assertThat(messageData.getText()).isEqualTo(text);
        assertThat(messageData.getCreatedDate()).isNotNull();
        assertThat(messageData.getModifiedDate()).isNotNull();
    }

    @Test
    void constructor_withParameters_shouldInitializeCollections() {
        // When
        MessageData messageData = new MessageData("adv-1", "msg-1", "Text");

        // Then
        assertThat(messageData.getTranslations()).isNotNull().isInstanceOf(HashMap.class);
        assertThat(messageData.getTags()).isNotNull().isInstanceOf(HashSet.class);
    }

    @Test
    void touch_shouldUpdateModifiedDate() throws InterruptedException {
        // Given
        MessageData messageData = new MessageData();
        Instant originalModifiedDate = messageData.getModifiedDate();

        // Small delay to ensure time difference
        Thread.sleep(10);

        // When
        messageData.touch();

        // Then
        assertThat(messageData.getModifiedDate()).isAfter(originalModifiedDate);
        assertThat(messageData.getCreatedDate()).isEqualTo(messageData.getCreatedDate()); // Created date unchanged
    }

    @Test
    void setters_shouldWorkCorrectly() {
        // Given
        MessageData messageData = new MessageData();

        // When
        messageData.setAdventureId("adv-123");
        messageData.setMessageId("msg-123");
        messageData.setText("Message text");
        messageData.setCategory("greetings");
        messageData.setNotes("Some notes");

        // Then
        assertThat(messageData.getAdventureId()).isEqualTo("adv-123");
        assertThat(messageData.getMessageId()).isEqualTo("msg-123");
        assertThat(messageData.getText()).isEqualTo("Message text");
        assertThat(messageData.getCategory()).isEqualTo("greetings");
        assertThat(messageData.getNotes()).isEqualTo("Some notes");
    }

    @Test
    void translations_shouldBeModifiable() {
        // Given
        MessageData messageData = new MessageData();

        // When
        messageData.getTranslations().put("de", "Willkommen!");
        messageData.getTranslations().put("fr", "Bienvenue!");

        // Then
        assertThat(messageData.getTranslations()).hasSize(2);
        assertThat(messageData.getTranslations()).containsEntry("de", "Willkommen!");
        assertThat(messageData.getTranslations()).containsEntry("fr", "Bienvenue!");
    }

    @Test
    void tags_shouldBeModifiable() {
        // Given
        MessageData messageData = new MessageData();

        // When
        messageData.getTags().add("intro");
        messageData.getTags().add("quest");
        messageData.getTags().add("important");

        // Then
        assertThat(messageData.getTags()).hasSize(3);
        assertThat(messageData.getTags()).containsExactlyInAnyOrder("intro", "quest", "important");
    }

    @Test
    void timestamps_shouldBeInstantType() {
        // Given
        MessageData messageData = new MessageData();

        // Then
        assertThat(messageData.getCreatedDate()).isInstanceOf(Instant.class);
        assertThat(messageData.getModifiedDate()).isInstanceOf(Instant.class);
    }

    @Test
    void createdDate_shouldNotChangeAfterTouch() {
        // Given
        MessageData messageData = new MessageData();
        Instant originalCreatedDate = messageData.getCreatedDate();

        // When
        messageData.touch();

        // Then
        assertThat(messageData.getCreatedDate()).isEqualTo(originalCreatedDate);
    }

    @Test
    void modifiedDate_shouldBeSetOnCreation() {
        // When
        MessageData messageData = new MessageData();

        // Then
        assertThat(messageData.getModifiedDate()).isNotNull();
        assertThat(messageData.getModifiedDate()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void setTranslations_shouldReplaceExistingMap() {
        // Given
        MessageData messageData = new MessageData();
        messageData.getTranslations().put("en", "Hello");

        HashMap<String, String> newTranslations = new HashMap<>();
        newTranslations.put("de", "Hallo");
        newTranslations.put("fr", "Bonjour");

        // When
        messageData.setTranslations(newTranslations);

        // Then
        assertThat(messageData.getTranslations()).hasSize(2);
        assertThat(messageData.getTranslations()).doesNotContainKey("en");
        assertThat(messageData.getTranslations()).containsKeys("de", "fr");
    }

    @Test
    void setTags_shouldReplaceExistingSet() {
        // Given
        MessageData messageData = new MessageData();
        messageData.getTags().add("old-tag");

        HashSet<String> newTags = new HashSet<>();
        newTags.add("new-tag-1");
        newTags.add("new-tag-2");

        // When
        messageData.setTags(newTags);

        // Then
        assertThat(messageData.getTags()).hasSize(2);
        assertThat(messageData.getTags()).doesNotContain("old-tag");
        assertThat(messageData.getTags()).containsExactlyInAnyOrder("new-tag-1", "new-tag-2");
    }
}
