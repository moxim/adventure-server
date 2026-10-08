package com.pdg.adventure.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The runtime state of a game at one moment, embedded in {@link SavedGameData}: ids and values only, so it can be
 * applied to a freshly loaded world. Ids are the ULIDs/UUIDs of the adventure's items, containers and locations;
 * variable names follow the same rules as in {@link VariableData} (no '.' or leading '$'), so every map key is
 * safe as a Mongo field name.
 */
@Data
public class GameSnapshotData {

    private String currentLocationId;
    private String currentPictureId;
    /** Every item id registered when the game was saved; an item in none of {@link #containers} was "nowhere". */
    private Set<String> knownItemIds = new HashSet<>();
    /** Container id -> the ids of its contents, in order. */
    private Map<String, List<String>> containers = new HashMap<>();
    private Set<String> wornItemIds = new HashSet<>();
    /** Item id -> lumen, for items whose lumen is not 0. */
    private Map<String, Integer> lumen = new HashMap<>();
    /** Location id -> times visited. */
    private Map<String, Integer> visits = new HashMap<>();
    private Map<String, Integer> variables = new HashMap<>();

    public GameSnapshotData() {
    }

    public List<String> containerContents(String aContainerId) {
        return containers.computeIfAbsent(aContainerId, _ -> new ArrayList<>());
    }
}
