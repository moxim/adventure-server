package com.pdg.adventure.server.storage.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pdg.adventure.model.PictureData;

@Repository
public interface PictureRepository extends MongoRepository<PictureData, String> {
}
