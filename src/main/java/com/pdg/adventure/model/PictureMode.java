package com.pdg.adventure.model;

/**
 * Whether a location's picture is shown when the player arrives there, set by the PictureMode action. A Look or a
 * Picture action shows a picture in every mode.
 */
public enum PictureMode {
    /** The location's picture is shown on every arrival, however often the location was visited. */
    ON,
    /** No picture is shown on arrival (the one showing is cleared). */
    OFF,
    /** The location's picture is shown on the first arrival only; on later arrivals the picture is cleared. */
    DEFAULT
}
