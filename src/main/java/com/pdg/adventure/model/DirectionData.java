package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.pdg.adventure.model.basic.BasicData;
import com.pdg.adventure.model.basic.DescriptionData;

/**
 * Persistent representation of a direction (exit) from a location.
 * A direction has exactly one {@link CommandData} — the command the player issues to follow
 * it (e.g. "go north"). It no longer inherits {@code ThingData} or {@code ItemData} because
 * it does not carry a {@code CommandProviderData} map; the single command is sufficient.
 */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class DirectionData extends BasicData {
    private DescriptionData descriptionData = new DescriptionData();
    private String destinationId;
    private boolean destinationMustBeMentioned;
    private CommandData commandData = new CommandData();
}
