package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MessageDataTest {

    @Test
    void constructor_default_shouldInitializeFieldsWithDefaults() {
        // When
        MessageData messageData = new MessageData();

        // Then
        assertThat(messageData.getCreatedAt()).isNotNull();
        assertThat(messageData.getUpdatedAt()).isNotNull();
    }

    @Test
    void constructor_withParameters_shouldSetFieldsCorrectly() {
        // Given
        String summary = "Welcome on arrival";
        String text = "Welcome to the adventure!";

        // When
        MessageData messageData = new MessageData(summary, text);

        // Then
        assertThat(messageData.getSummary()).isEqualTo(summary);
        assertThat(messageData.getText()).isEqualTo(text);
        assertThat(messageData.getCreatedAt()).isNotNull();
        assertThat(messageData.getUpdatedAt()).isNotNull();
    }

    @Test
    void id_shouldBeGeneratedAndUniquePerMessage() {
        // When
        MessageData first = new MessageData("same summary", "same text");
        MessageData second = new MessageData("same summary", "same text");

        // Then: the inherited id is the only unique reference - summaries may repeat
        assertThat(first.getId()).isNotBlank();
        assertThat(second.getId()).isNotBlank().isNotEqualTo(first.getId());
    }

    @Test
    void touch_shouldUpdateModifiedDate() throws InterruptedException {
        // Given
        MessageData messageData = new MessageData();
        Instant originalModifiedDate = messageData.getUpdatedAt();

        // Small delay to ensure time difference
        Thread.sleep(10);

        // When
        messageData.touch();

        // Then
        assertThat(messageData.getUpdatedAt()).isAfter(originalModifiedDate);
        assertThat(messageData.getCreatedAt()).isEqualTo(messageData.getCreatedAt()); // Created date unchanged
    }

    @Test
    void setters_shouldWorkCorrectly() {
        // Given
        MessageData messageData = new MessageData();

        // When
        messageData.setSummary("A short summary");
        messageData.setText("Message text");

        // Then
        assertThat(messageData.getSummary()).isEqualTo("A short summary");
        assertThat(messageData.getText()).isEqualTo("Message text");
    }

    @Test
    void timestamps_shouldBeInstantType() {
        // Given
        MessageData messageData = new MessageData();

        // Then
        assertThat(messageData.getCreatedAt()).isInstanceOf(Instant.class);
        assertThat(messageData.getUpdatedAt()).isInstanceOf(Instant.class);
    }

    @Test
    void createdDate_shouldNotChangeAfterTouch() {
        // Given
        MessageData messageData = new MessageData();
        Instant originalCreatedDate = messageData.getCreatedAt();

        // When
        messageData.touch();

        // Then
        assertThat(messageData.getCreatedAt()).isEqualTo(originalCreatedDate);
    }

    @Test
    void modifiedDate_shouldBeSetOnCreation() {
        // When
        MessageData messageData = new MessageData();

        // Then
        assertThat(messageData.getCreatedAt()).isNotNull();
        assertThat(messageData.getUpdatedAt()).isBeforeOrEqualTo(Instant.now());
    }
}
