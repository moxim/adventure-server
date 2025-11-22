package com.pdg.adventure.view.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationMessageTest {

    private ValidationMessage validationMessage;

    @BeforeEach
    void setUp() {
        validationMessage = new ValidationMessage();
    }

    @Test
    void constructor_shouldCreateInvisibleComponent() {
        // Then
        assertThat(validationMessage.isVisible()).isFalse();
        assertThat(validationMessage.getText()).isEmpty();
    }

    @Test
    void setText_withNonEmptyText_shouldMakeVisible() {
        // When
        validationMessage.setText("This is an error message");

        // Then
        assertThat(validationMessage.isVisible()).isTrue();
        assertThat(validationMessage.getText()).isEqualTo("This is an error message");
    }

    @Test
    void setText_withNull_shouldMakeInvisible() {
        // Given
        validationMessage.setText("Error message");
        assertThat(validationMessage.isVisible()).isTrue();

        // When
        validationMessage.setText(null);

        // Then
        assertThat(validationMessage.isVisible()).isFalse();
    }

    @Test
    void setText_withEmptyString_shouldMakeInvisible() {
        // Given
        validationMessage.setText("Error message");
        assertThat(validationMessage.isVisible()).isTrue();

        // When
        validationMessage.setText("");

        // Then
        assertThat(validationMessage.isVisible()).isFalse();
    }

    @Test
    void setText_multipleTimesWithDifferentValues_shouldUpdateCorrectly() {
        // When & Then
        validationMessage.setText("First error");
        assertThat(validationMessage.isVisible()).isTrue();
        assertThat(validationMessage.getText()).isEqualTo("First error");

        validationMessage.setText("Second error");
        assertThat(validationMessage.isVisible()).isTrue();
        assertThat(validationMessage.getText()).isEqualTo("Second error");

        validationMessage.setText("");
        assertThat(validationMessage.isVisible()).isFalse();

        validationMessage.setText("Third error");
        assertThat(validationMessage.isVisible()).isTrue();
        assertThat(validationMessage.getText()).isEqualTo("Third error");
    }

    @Test
    void getText_withoutSettingText_shouldReturnEmptyString() {
        // When
        String text = validationMessage.getText();

        // Then
        assertThat(text).isEmpty();
    }
}
