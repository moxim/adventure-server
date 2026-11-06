package com.pdg.adventure.model;

/**
 * One thing a PLAY action asks the browser to do: play the sound {@code times} times in a row, or, for 0, stop every
 * copy of it that is playing. A sound is an event, not state like the picture on show, so these are queued and handed
 * over once.
 */
public record SoundInstruction(String soundId, int times) {
}
