package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class AdventureFontTest {

    private static final String STYLESHEET = "/META-INF/resources/styles/adventure-fonts.css";
    private static final Pattern FONT_FACE_FAMILY = Pattern.compile("font-family:\\s*\"([^\"]+)\"");
    private static final Pattern FONT_FACE_URL = Pattern.compile("url\\(\"([^\"]+)\"\\)");
    private static final List<String> GENERIC_FAMILIES = List.of("sans-serif", "serif", "monospace", "fantasy");

    @Test
    void constantNamesAreStableBecauseTheyArePersisted() {
        assertThat(Arrays.stream(AdventureFont.values()).map(Enum::name))
                .containsExactly("DEFAULT", "INTER", "LORA", "IBM_PLEX_MONO", "MEDIEVAL_SHARP", "CINZEL", "OXANIUM", "SHARE_TECH_MONO",
                              "SPECIAL_ELITE", "IM_FELL_ENGLISH", "COURIER_PRIME");
    }

    @Test
    void defaultAppliesNoOverrideSoExistingAdventuresLookAsBefore() {
        assertThat(AdventureFont.DEFAULT.cssFontFamily()).isEmpty();
    }

    @Test
    void everyFontHasAUniqueLabelAndAGenericFallbackFamily() {
        assertThat(Arrays.stream(AdventureFont.values()).map(AdventureFont::label))
                .doesNotContainNull().doesNotHaveDuplicates().allMatch(label -> !label.isBlank());

        assertSoftly(softly -> Arrays.stream(AdventureFont.values())
                .filter(font -> font != AdventureFont.DEFAULT).forEach(font -> {
                    String stack = font.cssFontFamily().orElseThrow();
                    String last = stack.substring(stack.lastIndexOf(',') + 1).trim();
                    softly.assertThat(GENERIC_FAMILIES).as("fallback of %s", font).contains(last);
                }));
    }

    @Test
    void everyNonDefaultFontIsDeclaredInTheStylesheetAndItsFilesExist() throws IOException {
        String css = readClasspath(STYLESHEET);
        Matcher families = FONT_FACE_FAMILY.matcher(css);
        List<String> declared = families.results().map(m -> m.group(1)).distinct().toList();

        assertSoftly(softly -> {
            Arrays.stream(AdventureFont.values()).filter(font -> font != AdventureFont.DEFAULT).forEach(font -> {
                String stack = font.cssFontFamily().orElseThrow();
                String first = stack.substring(0, stack.indexOf(',')).replace("\"", "").trim();
                softly.assertThat(declared).as("@font-face family for %s", font).contains(first);
            });

            // a mistyped file name fails silently in the browser (it just falls back), so check every url
            Matcher urls = FONT_FACE_URL.matcher(css);
            List<String> files = urls.results().map(m -> m.group(1)).toList();
            softly.assertThat(files).as("font files referenced").isNotEmpty();
            files.forEach(file -> softly.assertThat(
                    AdventureFontTest.class.getResource("/META-INF/resources/styles/" + file))
                                        .as("font file %s", file).isNotNull());
        });
    }

    @Test
    void everyBundledFontShipsItsLicenceText() {
        assertSoftly(softly -> List.of("inter", "lora", "ibm-plex-mono", "medievalsharp", "cinzel", "oxanium", "share-tech-mono",
                    "special-elite", "im-fell-english", "courier-prime").forEach(slug ->
                softly.assertThat(AdventureFontTest.class.getResource(
                              "/META-INF/resources/styles/fonts/LICENSE-" + slug + ".txt"))
                      .as("licence of %s", slug).isNotNull()));
    }

    private static String readClasspath(String path) throws IOException {
        try (InputStream in = AdventureFontTest.class.getResourceAsStream(path)) {
            assertThat(in).as(path).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
