package com.pdg.adventure.server.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceholderSpecTest {

    @Test
    void of_countsPlainPlaceholder() {
        assertThat(PlaceholderSpec.of("You can't wear %s.").argumentPositions()).containsExactly(1);
    }

    @Test
    void of_countsPositionalPlaceholders() {
        assertThat(PlaceholderSpec.of("You put %1$s into %2$s.").argumentPositions()).containsExactly(1, 2);
    }

    @Test
    void of_returnsEmptyForNoPlaceholders() {
        assertThat(PlaceholderSpec.of("Exits are:").argumentPositions()).isEmpty();
    }

    @Test
    void of_returnsEmptyForNull() {
        assertThat(PlaceholderSpec.of(null).argumentPositions()).isEmpty();
    }

    @Test
    void of_ignoresEscapedPercent() {
        assertThat(PlaceholderSpec.of("100%% done, %s left.").argumentPositions()).containsExactly(1);
    }

    @Test
    void of_rejectsMixedStyles() {
        assertThatThrownBy(() -> PlaceholderSpec.of("%s and %1$s"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void satisfies_isReflexiveEvenForAGappedPositionSet() {
        // A spec built from e.g. "%3$s alone" (positions={3}, no 1 or 2) must still satisfy
        // itself - satisfies() compares position sets directly rather than rebuilding a
        // "1..max" range, so a gap in the original can't make it fail against its own text.
        PlaceholderSpec gapped = PlaceholderSpec.of("%3$s alone");
        assertThat(gapped.satisfies(gapped)).isTrue();
    }

    @Test
    void satisfies_acceptsReorderedPositionalMatch() {
        PlaceholderSpec original = PlaceholderSpec.of("%1$s into %2$s");
        PlaceholderSpec candidate = PlaceholderSpec.of("%2$s enthaelt %1$s");
        assertThat(candidate.satisfies(original)).isTrue();
    }

    @Test
    void satisfies_rejectsDroppedIndex() {
        PlaceholderSpec original = PlaceholderSpec.of("%1$s into %2$s");
        PlaceholderSpec candidate = PlaceholderSpec.of("%1$s and %1$s");
        assertThat(candidate.satisfies(original)).isFalse();
    }

    @Test
    void satisfies_rejectsOutOfRangeIndex() {
        PlaceholderSpec original = PlaceholderSpec.of("%1$s into %2$s");
        PlaceholderSpec candidate = PlaceholderSpec.of("%1$s, %2$s, %3$s");
        assertThat(candidate.satisfies(original)).isFalse();
    }

    @Test
    void isValidReplacement_allowsReorderingPositionalArguments() {
        assertThat(PlaceholderSpec.isValidReplacement("You put %1$s into %2$s.", "%2$s bekommt %1$s hinein."))
                .isTrue();
    }

    @Test
    void isValidReplacement_rejectsWrongPlaceholderCount() {
        assertThat(PlaceholderSpec.isValidReplacement("You can't wear %s.", "You can't wear that."))
                .isFalse();
    }

    @Test
    void isValidReplacement_returnsFalseInsteadOfThrowingOnMixedStyles() {
        assertThat(PlaceholderSpec.isValidReplacement("%s", "%s and %1$s")).isFalse();
    }
}
