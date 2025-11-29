package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

import com.pdg.adventure.model.basic.DatedData;
import com.pdg.adventure.server.storage.mongo.CascadeDelete;
import com.pdg.adventure.server.storage.mongo.CascadeSave;

@Document(collection = "adventures")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AdventureData extends DatedData {
    private String title;

    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private ItemContainerData playerPocket;

    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private Map<String, LocationData> locationData;
    private String currentLocationId;

    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private transient VocabularyData vocabularyData;

    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private Map<String, MessageData> messages;

    private String notes = ""; // to outline a story or whatever

    // WorkflowData workFlow = new WorkflowData();

    public AdventureData() {
        this(new VocabularyData());
    }

    public AdventureData(VocabularyData aVocabularyData) {
        vocabularyData = aVocabularyData;
        playerPocket = new ItemContainerData("your pocket");
        locationData = new HashMap<>();
        messages = new HashMap<>();
        currentLocationId = "";
        title = "";
    }
}
