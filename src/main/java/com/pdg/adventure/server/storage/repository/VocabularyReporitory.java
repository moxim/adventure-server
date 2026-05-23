package com.pdg.adventure.server.storage.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pdg.adventure.model.VocabularyData;

@Repository
public interface VocabularyReporitory extends MongoRepository<VocabularyData, String> {
}
