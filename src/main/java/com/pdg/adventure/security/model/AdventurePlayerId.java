package com.pdg.adventure.security.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AdventurePlayerId implements Serializable {

    @Column(name = "adventure_id", length = 26, nullable = false)
    private String adventureId;

    @Column(name = "user_id", length = 26, nullable = false)
    private String userId;

    protected AdventurePlayerId() {}

    public AdventurePlayerId(String adventureId, String userId) {
        this.adventureId = adventureId;
        this.userId = userId;
    }

    public String getAdventureId() {
        return adventureId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdventurePlayerId other)) return false;
        return Objects.equals(adventureId, other.adventureId)
               && Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adventureId, userId);
    }
}
