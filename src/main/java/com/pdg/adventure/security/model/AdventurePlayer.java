package com.pdg.adventure.security.model;

import jakarta.persistence.*;

/**
 * Tracks which PLAYERs have been assigned to a given Adventure (identified by its MongoDB ULID).
 * The composite PK (adventure_id, user_id) prevents duplicate assignments.
 */
@Entity
@Table(name = "adventure_players")
public class AdventurePlayer {

    @EmbeddedId
    private AdventurePlayerId id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserData user;

    protected AdventurePlayer() {}

    public AdventurePlayer(String adventureId, UserData user) {
        this.id = new AdventurePlayerId(adventureId, user.getId());
        this.user = user;
    }

    public AdventurePlayerId getId() {
        return id;
    }

    public String getAdventureId() {
        return id.getAdventureId();
    }

    public UserData getUser() {
        return user;
    }
}
