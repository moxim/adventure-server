package com.pdg.adventure.server.storage.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.pdg.adventure.model.SavedGameData;

@Repository
public interface SavedGameRepository extends MongoRepository<SavedGameData, String> {

    List<SavedGameData> findByUserIdAndAdventureId(String aUserId, String anAdventureId);
}
