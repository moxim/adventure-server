package com.pdg.adventure.api;

/**
 * Combines {@link Describable} and {@link HasCommands}: something that can be described
 * and that responds to commands.
 */
public interface Actionable extends Describable, HasCommands {
}
