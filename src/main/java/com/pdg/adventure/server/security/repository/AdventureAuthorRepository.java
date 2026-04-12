package com.pdg.adventure.server.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.pdg.adventure.security.model.AdventureAuthor;
import com.pdg.adventure.security.model.UserData;

@Repository
public interface AdventureAuthorRepository extends JpaRepository<AdventureAuthor, String> {

    Optional<AdventureAuthor> findByAdventureId(String adventureId);

    List<AdventureAuthor> findByUser(UserData user);

    boolean existsByAdventureId(String adventureId);

    void deleteByAdventureId(String adventureId);
}
