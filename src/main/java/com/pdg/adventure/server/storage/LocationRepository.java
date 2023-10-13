package com.pdg.adventure.server.storage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pdg.adventure.model.LocationData;

@Repository
public interface LocationRepository extends MongoRepository<LocationData, String> {
}
