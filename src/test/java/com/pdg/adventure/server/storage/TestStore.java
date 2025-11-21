package com.pdg.adventure.server.storage;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestStore extends MongoRepository<Parent, String> {

}


@Document
@Data
class Parent {
    @Id
    String id;

    @DBRef
    Child[] children;
}

@Document
@Data
class Child {
    @Id
    String id;

    @DBRef
    Parent parent;
}
