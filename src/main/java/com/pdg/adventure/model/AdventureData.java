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

    // The builder version that last wrote this adventure (null until the first save by a version that stamps it);
    // see AdventureService.saveAdventureData. Saved games record it so load can warn about a mismatch.
    private String builderVersion;

    // Variable names are free text and may contain characters that are
    // illegal in Mongo field names ('.', leading '$').
    private VariableData variableData;

    private String notes; // to outline a story or whatever

    private WorkflowData workflowData;

    // The font of the run view's game text. Stored as the constant's name; documents saved before fonts
    // existed have no such field and keep the constructor's DEFAULT.
    private AdventureFont font;

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
        font = AdventureFont.DEFAULT;
    }

    /** Never null: an absent or null stored value means {@link AdventureFont#DEFAULT}. */
    public AdventureFont getFont() {
        return font == null ? AdventureFont.DEFAULT : font;
    }

    public void setFont(AdventureFont aFont) {
        font = aFont == null ? AdventureFont.DEFAULT : aFont;
    }

    /** The names of all variables defined for this adventure. */
    public List<String> variableNames() {
        return variableData.getVariableNames();
    }

}
