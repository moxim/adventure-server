package com.pdg.adventure.server.storage;

import com.pdg.adventure.model.VocabularyData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyReporitory extends MongoRepository<VocabularyData, String> {
}
