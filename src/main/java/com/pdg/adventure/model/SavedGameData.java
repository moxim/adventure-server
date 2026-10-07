package com.pdg.adventure.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * One saved game of one player in one adventure slot. The id is deterministic ({@link #idFor}), so Mongo itself
 * keeps it to one save per slot and saving into a slot is a plain upsert.
 */
@Document(collection = "savedgames")
@Data
public class SavedGameData {

    public static final int SAVED_GAME_SLOTS = 10;

    @Id
    private String id;
    private String userId;
    private String adventureId;
    private int slot;
    private Instant savedAt;
    /** The builder version the adventure carried when this was saved; null when unknown. */
    private String builderVersion;
    private GameSnapshotData snapshot;

    public static String idFor(String aUserId, String anAdventureId, int aSlot) {
        return aUserId + ":" + anAdventureId + ":" + aSlot;
    }
}
