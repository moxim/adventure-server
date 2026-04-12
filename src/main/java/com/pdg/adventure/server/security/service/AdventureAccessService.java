package com.pdg.adventure.server.security.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.AdventureAuthor;
import com.pdg.adventure.security.model.AdventurePlayer;
import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.repository.AdventureAuthorRepository;
import com.pdg.adventure.server.security.repository.AdventurePlayerRepository;
import com.pdg.adventure.server.storage.service.AdventureService;

/**
 * Enforces adventure access rules:
 * - ADMIN  : full access to all adventures; can assign an AUTHOR to any orphaned adventure
 *            and assign/remove PLAYERs
 * - AUTHOR : CRUD on adventures they own; auto-assigned as author on creation
 * - PLAYER : read-only on adventures they have been assigned to
 *
 * Note: MongoDB (adventure data) and MySQL (assignments) cannot share a single ACID transaction.
 * Methods that write to both stores are annotated @Transactional to guard the MySQL side; the
 * MongoDB write is performed first so that the adventure ID is available for the assignment row.
 */
@Service
public class AdventureAccessService {

    private final AdventureService adventureService;
    private final AdventureAuthorRepository authorRepository;
    private final AdventurePlayerRepository playerRepository;

    public AdventureAccessService(AdventureService adventureService,
                                  AdventureAuthorRepository authorRepository,
                                  AdventurePlayerRepository playerRepository) {
        this.adventureService = adventureService;
        this.authorRepository = authorRepository;
        this.playerRepository = playerRepository;
    }

    // -------------------------------------------------------------------------
    // Access checks
    // -------------------------------------------------------------------------

    public boolean canRead(String adventureId, UserData user) {
        if (user.getRoles().contains(Role.ADMIN)) return true;
        if (isAssignedAuthor(adventureId, user)) return true;
        return playerRepository.existsByIdAdventureIdAndUser(adventureId, user);
    }

    public boolean canWrite(String adventureId, UserData user) {
        if (user.getRoles().contains(Role.ADMIN)) return true;
        return isAssignedAuthor(adventureId, user);
    }

    private boolean isAssignedAuthor(String adventureId, UserData user) {
        return authorRepository.findByAdventureId(adventureId)
                               .map(a -> a.getUser().getId().equals(user.getId()))
                               .orElse(false);
    }

    // -------------------------------------------------------------------------
    // Adventure lifecycle (writes to both MongoDB and MySQL)
    // -------------------------------------------------------------------------

    /**
     * Returns the adventure if the user can read it, empty otherwise.
     */
    public Optional<AdventureData> findAdventureById(String adventureId, UserData user) {
        if (!canRead(adventureId, user)) {
            return Optional.empty();
        }
        return adventureService.findAdventureById(adventureId);
    }

    /**
     * Saves changes to an existing adventure. Requires write access.
     */
    public void saveAdventureData(AdventureData adventure, UserData user) {
        if (!canWrite(adventure.getId(), user)) {
            throw new AccessDeniedException("No write access to adventure: " + adventure.getId());
        }
        adventureService.saveAdventureData(adventure);
    }

    /**
     * Saves the adventure to MongoDB and auto-assigns the given user as its AUTHOR in MySQL.
     * Call this instead of saveAdventureData when creating a new adventure.
     */
    @Transactional
    public AdventureData createAdventure(AdventureData adventure, UserData author) {
        adventureService.saveAdventureData(adventure);
        authorRepository.save(new AdventureAuthor(adventure.getId(), author));
        return adventure;
    }

    /**
     * Deletes the adventure from MongoDB and removes all author/player assignments from MySQL.
     * Requires ADMIN role or AUTHOR ownership.
     */
    @Transactional
    public void deleteAdventure(String adventureId, UserData user) {
        if (!canWrite(adventureId, user)) {
            throw new AccessDeniedException("No write access to adventure: " + adventureId);
        }
        authorRepository.deleteByAdventureId(adventureId);
        playerRepository.deleteByIdAdventureId(adventureId);
        adventureService.deleteAdventure(adventureId);
    }

    // -------------------------------------------------------------------------
    // Assignment management (ADMIN only — callers must verify the role)
    // -------------------------------------------------------------------------

    /**
     * Assigns an AUTHOR to an adventure that currently has none.
     * Throws if the adventure already has an author.
     */
    @Transactional
    public void assignAuthor(String adventureId, UserData newAuthor) {
        if (authorRepository.existsByAdventureId(adventureId)) {
            throw new IllegalStateException(
                    "Adventure %s already has an author".formatted(adventureId));
        }
        authorRepository.save(new AdventureAuthor(adventureId, newAuthor));
    }

    @Transactional
    public void reassignAuthor(String adventureId, UserData newAuthor) {
        authorRepository.deleteByAdventureId(adventureId);
        authorRepository.save(new AdventureAuthor(adventureId, newAuthor));
    }

    @Transactional
    public void removeAuthor(String adventureId) {
        authorRepository.deleteByAdventureId(adventureId);
    }

    @Transactional
    public void assignPlayer(String adventureId, UserData player) {
        if (!playerRepository.existsByIdAdventureIdAndUser(adventureId, player)) {
            playerRepository.save(new AdventurePlayer(adventureId, player));
        }
    }

    @Transactional
    public void removePlayer(String adventureId, UserData player) {
        playerRepository.deleteByIdAdventureIdAndUser(adventureId, player);
    }

    // -------------------------------------------------------------------------
    // Assignment queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Optional<UserData> findAuthorForAdventure(String adventureId) {
        return authorRepository.findByAdventureId(adventureId)
                               .map(AdventureAuthor::getUser);
    }

    @Transactional(readOnly = true)
    public List<UserData> findPlayersForAdventure(String adventureId) {
        return playerRepository.findByIdAdventureId(adventureId).stream()
                               .map(AdventurePlayer::getUser)
                               .collect(java.util.stream.Collectors.toList());
    }

    /** Returns a map of adventureId → author username for all assigned adventures. */
    @Transactional(readOnly = true)
    public Map<String, String> getAuthorNamesByAdventureId() {
        return authorRepository.findAll().stream()
                               .collect(java.util.stream.Collectors.toMap(
                                       AdventureAuthor::getAdventureId,
                                       a -> a.getUser().getUsername()));
    }

    // -------------------------------------------------------------------------
    // Filtered adventure lists
    // -------------------------------------------------------------------------

    /**
     * Returns all adventures visible to the given user:
     * - ADMIN  → all adventures
     * - AUTHOR → adventures they authored
     * - PLAYER → adventures they were assigned to
     * A user with multiple roles gets the union (most permissive wins).
     */
    public List<AdventureData> getAdventuresForUser(UserData user) {
        if (user.getRoles().contains(Role.ADMIN)) {
            return adventureService.getAdventures();
        }

        List<String> ids = authorRepository.findByUser(user).stream()
                                           .map(AdventureAuthor::getAdventureId)
                                           .collect(java.util.stream.Collectors.toList());

        playerRepository.findByUser(user).stream()
                        .map(AdventurePlayer::getAdventureId)
                        .filter(id -> !ids.contains(id))
                        .forEach(ids::add);

        return adventureService.getAdventuresByIds(ids);
    }
}
