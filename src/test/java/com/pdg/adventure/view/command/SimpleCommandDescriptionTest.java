package com.pdg.adventure.view.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleCommandDescriptionTest {

    @Test
    void constructor_withThreeParameters_shouldSetAllFields() {
        // Given
        String verb = "get";
        String adjective = "rusty";
        String noun = "key";

        // When
        SimpleCommandDescription description = new SimpleCommandDescription(verb, adjective, noun);

        // Then
        assertThat(description.getVerb()).isEqualTo(verb);
        assertThat(description.getAdjective()).isEqualTo(adjective);
        assertThat(description.getNoun()).isEqualTo(noun);
    }

    @Test
    void constructor_withCommandSpecification_shouldParseCorrectly() {
        // Given
        String commandSpec = "get|rusty|key";

        // When
        SimpleCommandDescription description = new SimpleCommandDescription(commandSpec);

        // Then
        assertThat(description.getVerb()).isEqualTo("get");
        assertThat(description.getAdjective()).isEqualTo("rusty");
        assertThat(description.getNoun()).isEqualTo("key");
    }

    @Test
    void constructor_withCommandSpecificationWithWhitespace_shouldParseCorrectly() {
        // Given
        String commandSpec = "take|old|coin";

        // When
        SimpleCommandDescription description = new SimpleCommandDescription(commandSpec);

        // Then
        assertThat(description.getVerb()).isEqualTo("take");
        assertThat(description.getAdjective()).isEqualTo("old");
        assertThat(description.getNoun()).isEqualTo("coin");
    }

    @Test
    void constructor_withThreeParameters_shouldAllowNullValues() {
        // When
        SimpleCommandDescription description = new SimpleCommandDescription(null, null, null);

        // Then
        assertThat(description.getVerb()).isNull();
        assertThat(description.getAdjective()).isNull();
        assertThat(description.getNoun()).isNull();
    }

    @Test
    void constructor_withThreeParameters_shouldAllowEmptyStrings() {
        // When
        SimpleCommandDescription description = new SimpleCommandDescription("", "", "");

        // Then
        assertThat(description.getVerb()).isEmpty();
        assertThat(description.getAdjective()).isEmpty();
        assertThat(description.getNoun()).isEmpty();
    }

    @Test
    void setters_shouldModifyFields() {
        // Given
        SimpleCommandDescription description = new SimpleCommandDescription("get", "rusty", "key");

        // When
        description.setVerb("take");
        description.setAdjective("golden");
        description.setNoun("sword");

        // Then
        assertThat(description.getVerb()).isEqualTo("take");
        assertThat(description.getAdjective()).isEqualTo("golden");
        assertThat(description.getNoun()).isEqualTo("sword");
    }

    @Test
    void equals_shouldReturnTrue_forSameValues() {
        // Given
        SimpleCommandDescription desc1 = new SimpleCommandDescription("get", "rusty", "key");
        SimpleCommandDescription desc2 = new SimpleCommandDescription("get", "rusty", "key");

        // Then
        assertThat(desc1).isEqualTo(desc2);
    }

    @Test
    void equals_shouldReturnFalse_forDifferentValues() {
        // Given
        SimpleCommandDescription desc1 = new SimpleCommandDescription("get", "rusty", "key");
        SimpleCommandDescription desc2 = new SimpleCommandDescription("take", "golden", "sword");

        // Then
        assertThat(desc1).isNotEqualTo(desc2);
    }

    @Test
    void hashCode_shouldBeSame_forEqualObjects() {
        // Given
        SimpleCommandDescription desc1 = new SimpleCommandDescription("get", "rusty", "key");
        SimpleCommandDescription desc2 = new SimpleCommandDescription("get", "rusty", "key");

        // Then
        assertThat(desc1.hashCode()).isEqualTo(desc2.hashCode());
    }

    @Test
    void toString_shouldIncludeAllFields() {
        // Given
        SimpleCommandDescription description = new SimpleCommandDescription("get", "rusty", "key");

        // When
        String result = description.toString();

        // Then
        assertThat(result).contains("get");
        assertThat(result).contains("rusty");
        assertThat(result).contains("key");
    }
}
