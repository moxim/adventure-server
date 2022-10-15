package com.pdg.adventure.server.storage;

import com.pdg.adventure.server.location.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationRepository extends MongoRepository<Location, String> {
}
