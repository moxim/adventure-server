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
    private String title ="";
    private ItemContainerData playerPocket = new ItemContainerData();
    @DBRef(lazy = false)
//    @CascadeSave
    private Map<String, LocationData> locationData = new HashMap<>();
//    private Set<LocationData> locationData = new HashSet<>();
    private String currentLocationId = "";
    @DBRef(lazy = false)
//    private Set<Word> words = new HashSet<>();
    private transient VocabularyData vocabularyData = new VocabularyData();

    private String notes = ""; // to outline a story or whatever

    // WorkflowData workFlow = new WorkflowData();
}
