package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pdg.adventure.model.basic.DatedData;
import com.pdg.adventure.server.storage.mongo.CascadeDelete;
import com.pdg.adventure.server.storage.mongo.CascadeSave;

@Document(collection = "adventures")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AdventureData extends DatedData {
    private String title;

    @DBRef(lazy = true)
    @CascadeSave
    @CascadeDelete
    private ItemContainerData playerPocket;

    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private Map<String, LocationData> locationData;

    @DBRef(lazy = false)
    @CascadeDelete
    private Map<String, PictureData> pictureData;
    private String currentLocationId;

    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private transient VocabularyData vocabularyData;

    // Embedded (not @DBRef): owned 1:1 by this adventure, never queried or shared independently -
    // see MessageData/SystemMessageData Javadoc.
    private Map<String, MessageData> messages;

    private Map<String, SystemMessageData> systemMessages;

    // Variable names are free text and may contain characters that are
    // illegal in Mongo field names ('.', leading '$').
    private VariableData variableData;

    private String notes; // to outline a story or whatever

    private WorkflowData workflowData;

    public AdventureData() {
        this(new VocabularyData());
    }

    public AdventureData(VocabularyData aVocabularyData) {
        title = "";
        playerPocket = new ItemContainerData("your pocket");
        locationData = new HashMap<>();
        pictureData = new HashMap<>();
        vocabularyData = aVocabularyData;
        messages = new HashMap<>();
        systemMessages = new HashMap<>();
        currentLocationId = "";
        notes = "";
        workflowData = new WorkflowData();
        variableData = new VariableData(new HashMap<>());
    }

    /** The names of all variables defined for this adventure. */
    public List<String> variableNames() {
        return variableData.getVariableNames();
    }

}
