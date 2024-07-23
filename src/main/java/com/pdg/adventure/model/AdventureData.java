package com.pdg.adventure.model;

import com.pdg.adventure.model.basics.BasicData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AdventureData extends BasicData {
    private String title;
    private ItemContainerData playerPocket;
    @DBRef(lazy = false)
//    @CascadeSave
    private Map<String, LocationData> locationData;
//    private Set<LocationData> locationData = new HashSet<>();
    private String currentLocationId;
    @DBRef(lazy = false)
    private transient VocabularyData vocabularyData;

    private String notes = ""; // to outline a story or whatever

    // WorkflowData workFlow = new WorkflowData();

    public AdventureData() {
        this(new VocabularyData());
    }

    public AdventureData(VocabularyData aVocabularyData) {
        vocabularyData = aVocabularyData;
        playerPocket = new ItemContainerData();
        locationData = new HashMap<>();
        currentLocationId = "";
        title = "";
    }
}
