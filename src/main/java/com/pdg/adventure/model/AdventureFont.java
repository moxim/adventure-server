package com.pdg.adventure.model;

import java.util.Optional;

/**
 * The font an author picks for running an adventure. Applied to the game text of the run view only
 * ({@code AdventureRunView}); the editors always use the application font.
 * <p>
 * The constant NAMES are persisted in {@link AdventureData#getFont()}: never rename or remove one, or every
 * adventure stored with it fails to load. Retire a font by hiding it from the editor's selection instead.
 * <p>
 * The web fonts are bundled (latin subset, SIL Open Font License; Special Elite is Apache 2.0) and declared in
 * {@code META-INF/resources/styles/adventure-fonts.css}; each family stack falls back to a system font,
 * which is also what shows for characters outside the latin subset.
 * <p>
 * TODO: Review needed — the font list (one clean sans, one serif, one terminal mono, two fantasy, two futuristic, three typewriter/old-print) and the
 * latin-only subset were my choices; adding a font means a new constant, @font-face rules and licence text.
 */
public enum AdventureFont {
    /** The application's own font: no override, adventures look exactly as they did before fonts existed. */
    DEFAULT("Default (system)", null),
    INTER("Inter (clean)", "\"Inter\", system-ui, sans-serif"),
    LORA("Lora (serif)", "\"Lora\", Georgia, serif"),
    IBM_PLEX_MONO("IBM Plex Mono (terminal)", "\"IBM Plex Mono\", ui-monospace, monospace"),
    MEDIEVAL_SHARP("MedievalSharp (fantasy)", "\"MedievalSharp\", Georgia, serif"),
    CINZEL("Cinzel (classical fantasy)", "\"Cinzel\", Georgia, serif"),
    OXANIUM("Oxanium (futuristic)", "\"Oxanium\", system-ui, sans-serif"),
    SHARE_TECH_MONO("Share Tech Mono (futuristic terminal)", "\"Share Tech Mono\", ui-monospace, monospace"),
    SPECIAL_ELITE("Special Elite (worn typewriter)", "\"Special Elite\", \"Courier New\", monospace"),
    IM_FELL_ENGLISH("IM Fell English (old print)", "\"IM Fell English\", Georgia, serif"),
    COURIER_PRIME("Courier Prime (typewriter)", "\"Courier Prime\", \"Courier New\", monospace");

    private final String label;
    private final String cssFontFamily;

    AdventureFont(String aLabel, String aCssFontFamily) {
        label = aLabel;
        cssFontFamily = aCssFontFamily;
    }

    public String label() {
        return label;
    }

    /** The CSS {@code font-family} value, empty for {@link #DEFAULT} (apply no override). */
    public Optional<String> cssFontFamily() {
        return Optional.ofNullable(cssFontFamily);
    }
}
