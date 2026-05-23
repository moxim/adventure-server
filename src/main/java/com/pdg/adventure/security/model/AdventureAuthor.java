package com.pdg.adventure.security.model;

import jakarta.persistence.*;

/**
 * Tracks which user is the AUTHOR of a given Adventure (identified by its MongoDB ULID).
 * The adventure_id is the primary key, enforcing the one-author-per-adventure rule at the DB level.
 */
@Entity
@Table(name = "adventure_authors")
public class AdventureAuthor {

    @Id
    @Column(name = "adventure_id", length = 26, nullable = false, updatable = false)
    private String adventureId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserData user;

    protected AdventureAuthor() {}

    public AdventureAuthor(String adventureId, UserData user) {
        this.adventureId = adventureId;
        this.user = user;
    }

    public String getAdventureId() {
        return adventureId;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }
}
