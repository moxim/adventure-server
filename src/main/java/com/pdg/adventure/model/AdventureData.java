package com.pdg.adventure.model;

import com.pdg.adventure.model.basics.BasicData;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AdventureData extends BasicData {
    private String title ="";
    private ItemContainerData playerPocket = new ItemContainerData();
    @DBRef(lazy = false)
//    @CascadeSave
    private Set<LocationData> locationData = new HashSet<>();
    private String currentLocationId = "";
    private Vocabulary vocabulary = new Vocabulary();
    // WorkflowData workFlow = new WorkflowData();
    private String notes = ""; // to outline a story or whatever
}
