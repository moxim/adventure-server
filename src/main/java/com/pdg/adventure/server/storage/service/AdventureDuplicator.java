package com.pdg.adventure.server.storage.service;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.pdg.adventure.server.storage.service.AdventureDocumentGraph.ADVENTURES;
import static com.pdg.adventure.server.storage.service.AdventureDocumentGraph.ID;

/**
 * Copies a complete adventure: the adventure document plus every document it reaches through a
 * {@code @DBRef} (locations, containers, items, pictures, vocabulary, words, ...).
 * <p>
 * The copy works on the raw documents, not on the {@code *Data} objects. Ids are fixed at
 * construction ({@code BasicData}) and {@code save} upserts by id, so a copy that kept any id
 * would silently overwrite the original's documents. Working on raw documents also copies
 * whatever the object model leaves out of cascade-save (e.g. pictures) or hides behind lazy
 * proxies, without knowing any of the classes.
 * <p>
 * Every document that gets copied receives a new id. All references between them are plain id
 * strings - {@code destinationId}, {@code currentLocationId}, {@code adventureId}, the keys of
 * the {@code locationData} map, the {@code $id} of each DBRef, ... - so one old-to-new id map,
 * applied to every string value and map key, rewires the whole copy. Documents embedded in a
 * copied document keep their ids: they are only meaningful inside their owner.
 * <p>
 * The graph walking itself lives in {@link AdventureDocumentGraph}, shared with export and import.
 */
@Service
public class AdventureDuplicator {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureDuplicator.class);

    private final AdventureDocumentGraph graph;

    public AdventureDuplicator(MongoTemplate aMongoTemplate) {
        graph = new AdventureDocumentGraph(aMongoTemplate);
    }

    /**
     * @return the id of the new adventure
     * @throws IllegalArgumentException if there is no adventure with the given id
     */
    public String duplicate(String aSourceAdventureId, String aNewTitle) {
        Document source = graph.findAdventure(aSourceAdventureId);
        if (source == null) {
            throw new IllegalArgumentException("Adventure not found: " + aSourceAdventureId);
        }

        Map<String, List<Document>> copies =
                AdventureDocumentGraph.copyWithNewIds(graph.collectReachableDocuments(source));

        Document copiedAdventure = copies.get(ADVENTURES).getFirst();
        copiedAdventure.put("title", aNewTitle);
        Date now = new Date();
        copiedAdventure.put("createdAt", now);
        copiedAdventure.put("updatedAt", now);

        graph.insertAll(copies);
        String copyId = copiedAdventure.get(ID).toString();
        LOG.info("Duplicated adventure {} as {} ({} documents)", aSourceAdventureId, copyId,
                 copies.values().stream().mapToInt(List::size).sum());
        return copyId;
    }
}
