package com.pdg.adventure.server.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.pdg.adventure.security.model.AdventurePlayer;
import com.pdg.adventure.security.model.AdventurePlayerId;
import com.pdg.adventure.security.model.UserData;

@Repository
public interface AdventurePlayerRepository extends JpaRepository<AdventurePlayer, AdventurePlayerId> {

    List<AdventurePlayer> findByIdAdventureId(String adventureId);

    List<AdventurePlayer> findByUser(UserData user);

    boolean existsByIdAdventureIdAndUser(String adventureId, UserData user);

    void deleteByIdAdventureId(String adventureId);

    void deleteByIdAdventureIdAndUser(String adventureId, UserData user);
}
