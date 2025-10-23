package com.pdg.adventure.model.basic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.mongo.CascadeSave;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BasicDescriptionData extends BasicData {
    @DBRef
    @CascadeSave
    private Word adjective;
    @DBRef
    @CascadeSave
    private Word noun;
}
