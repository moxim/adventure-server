package com.pdg.adventure.server.storage.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import com.pdg.adventure.model.GameSnapshotData;
import com.pdg.adventure.model.SavedGameData;
import com.pdg.adventure.server.storage.repository.SavedGameRepository;

/** The slot rules for saved games: up to {@link SavedGameData#SAVED_GAME_SLOTS} per player and adventure. */
@Service
public class SavedGameService {

    private static final DateTimeFormatter LABEL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SavedGameRepository repository;

    public SavedGameService(SavedGameRepository aRepository) {
        repository = aRepository;
    }

    public OptionalInt freeSlot(String aUserId, String anAdventureId) {
        Set<Integer> used = new HashSet<>();
        repository.findByUserIdAndAdventureId(aUserId, anAdventureId).forEach(saved -> used.add(saved.getSlot()));
        for (int slot = 1; slot <= SavedGameData.SAVED_GAME_SLOTS; slot++) {
            if (!used.contains(slot)) {
                return OptionalInt.of(slot);
            }
        }
        return OptionalInt.empty();
    }

    public SavedGameData save(String aUserId, String anAdventureId, int aSlot, String aBuilderVersion,
                              GameSnapshotData aSnapshot, Instant aSavedAt) {
        if (aSlot < 1 || aSlot > SavedGameData.SAVED_GAME_SLOTS) {
            throw new IllegalArgumentException("Slot out of range: " + aSlot);
        }
        SavedGameData saved = new SavedGameData();
        saved.setId(SavedGameData.idFor(aUserId, anAdventureId, aSlot));
        saved.setUserId(aUserId);
        saved.setAdventureId(anAdventureId);
        saved.setSlot(aSlot);
        saved.setSavedAt(aSavedAt);
        saved.setBuilderVersion(aBuilderVersion);
        saved.setSnapshot(aSnapshot);
        return repository.save(saved);
    }

    public List<SavedGameData> list(String aUserId, String anAdventureId) {
        return repository.findByUserIdAndAdventureId(aUserId, anAdventureId).stream()
                         .sorted(Comparator.comparingInt(SavedGameData::getSlot))
                         .toList();
    }

    public Optional<SavedGameData> find(String aUserId, String anAdventureId, int aSlot) {
        return repository.findById(SavedGameData.idFor(aUserId, anAdventureId, aSlot));
    }

    /** The slot number a player typed (1 to 10), or empty for anything else. */
    public static OptionalInt parseSlot(String aNoun) {
        if (aNoun == null || aNoun.isBlank()) {
            return OptionalInt.empty();
        }
        try {
            int slot = Integer.parseInt(aNoun.trim());
            return slot >= 1 && slot <= SavedGameData.SAVED_GAME_SLOTS ? OptionalInt.of(slot) : OptionalInt.empty();
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static String label(String anAdventureTitle, Instant aSavedAt) {
        return label(anAdventureTitle, aSavedAt, ZoneId.systemDefault());
    }

    public static String label(String anAdventureTitle, Instant aSavedAt, ZoneId aZone) {
        return "%s - %s".formatted(anAdventureTitle, LABEL_TIME.withZone(aZone).format(aSavedAt));
    }
}
