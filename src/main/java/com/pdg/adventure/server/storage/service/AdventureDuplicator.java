package com.pdg.adventure.server.storage.service;

import com.github.f4b6a3.ulid.Ulid;
import com.mongodb.DBRef;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 */
@Service
public class AdventureDuplicator {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureDuplicator.class);

    private static final String ADVENTURES = "adventures";
    private static final String ID = "_id";
    private static final String DBREF_COLLECTION = "$ref";
    private static final String DBREF_ID = "$id";

    private final MongoTemplate mongoTemplate;

    public AdventureDuplicator(MongoTemplate aMongoTemplate) {
        mongoTemplate = aMongoTemplate;
    }

    /**
     * @return the id of the new adventure
     * @throws IllegalArgumentException if there is no adventure with the given id
     */
    public String duplicate(String aSourceAdventureId, String aNewTitle) {
        Document source = mongoTemplate.getCollection(ADVENTURES)
                                       .find(new Document(ID, aSourceAdventureId)).first();
        if (source == null) {
            throw new IllegalArgumentException("Adventure not found: " + aSourceAdventureId);
        }

        // collection name -> documents to copy, the adventure itself included
        Map<String, List<Document>> originals = collectReachableDocuments(source);

        Map<String, String> idMap = new HashMap<>();
        originals.values().forEach(docs -> docs.forEach(doc -> idMap.put(doc.get(ID).toString(), newId())));

        Map<String, List<Document>> copies = new LinkedHashMap<>();
        originals.forEach((collection, docs) -> {
            List<Document> copied = new ArrayList<>(docs.size());
            docs.forEach(doc -> copied.add((Document) rewrite(doc, idMap)));
            copies.put(collection, copied);
        });

        Document copiedAdventure = copies.get(ADVENTURES).getFirst();
        copiedAdventure.put("title", aNewTitle);
        Date now = new Date();
        copiedAdventure.put("createdAt", now);
        copiedAdventure.put("updatedAt", now);

        insertAll(copies);
        String copyId = copiedAdventure.get(ID).toString();
        LOG.info("Duplicated adventure {} as {} ({} documents)", aSourceAdventureId, copyId,
                 copies.values().stream().mapToInt(List::size).sum());
        return copyId;
    }

    /** Follows every DBRef, transitively, starting at the adventure (which is listed first). */
    private Map<String, List<Document>> collectReachableDocuments(Document anAdventure) {
        Map<String, List<Document>> result = new LinkedHashMap<>();
        Map<String, Boolean> visited = new HashMap<>();
        Deque<DBRef> pending = new ArrayDeque<>();

        result.computeIfAbsent(ADVENTURES, _ -> new ArrayList<>()).add(anAdventure);
        visited.put(key(ADVENTURES, anAdventure.get(ID)), true);
        findReferences(anAdventure, pending);

        while (!pending.isEmpty()) {
            DBRef reference = pending.poll();
            if (visited.putIfAbsent(key(reference.getCollectionName(), reference.getId()), true) != null) {
                continue;
            }
            Document target = mongoTemplate.getCollection(reference.getCollectionName())
                                           .find(new Document(ID, reference.getId())).first();
            if (target == null) {
                // TODO: Review needed — a dangling DBRef is copied as-is (pointing at its old id); alternative: fail the copy
                LOG.warn("Dangling reference {} while duplicating an adventure", reference);
                continue;
            }
            result.computeIfAbsent(reference.getCollectionName(), _ -> new ArrayList<>()).add(target);
            findReferences(target, pending);
        }
        return result;
    }

    private void findReferences(Object aNode, Deque<DBRef> aPending) {
        switch (aNode) {
            case DBRef reference -> aPending.add(reference);
            case Document document when document.containsKey(DBREF_COLLECTION) && document.containsKey(DBREF_ID) ->
                    aPending.add(new DBRef(document.getString(DBREF_COLLECTION), document.get(DBREF_ID)));
            case Map<?, ?> map -> map.values().forEach(value -> findReferences(value, aPending));
            case Iterable<?> iterable -> iterable.forEach(value -> findReferences(value, aPending));
            case null, default -> { /* a leaf value */ }
        }
    }

    /** Deep copy of the node with every string value and map key that is an old id replaced by its new id. */
    private Object rewrite(Object aNode, Map<String, String> anIdMap) {
        return switch (aNode) {
            // TODO: Review needed — ids that are not strings (e.g. ObjectId) become string ids in the copy; none are created today
            case String text -> anIdMap.getOrDefault(text, text);
            case DBRef reference -> new DBRef(reference.getCollectionName(),
                                              anIdMap.getOrDefault(reference.getId().toString(), reference.getId().toString()));
            case Document document -> {
                Document copy = new Document();
                document.forEach((name, value) -> copy.put(anIdMap.getOrDefault(name, name), rewrite(value, anIdMap)));
                yield copy;
            }
            case List<?> list -> {
                List<Object> copy = new ArrayList<>(list.size());
                list.forEach(value -> copy.add(rewrite(value, anIdMap)));
                yield copy;
            }
            case Map<?, ?> map -> throw new IllegalStateException("Unexpected raw map in a BSON document: " + map);
            case null, default -> aNode; // numbers, dates, binary content, ...
        };
    }

    /** MongoDB cannot span a transaction here (standalone), so undo by hand if a collection fails. */
    private void insertAll(Map<String, List<Document>> aCopies) {
        try {
            aCopies.forEach((collection, docs) -> mongoTemplate.getCollection(collection).insertMany(docs));
        } catch (RuntimeException e) {
            // the new ids exist nowhere else, so removing by id cannot touch anything but the partial copy
            aCopies.forEach((collection, docs) -> mongoTemplate.getCollection(collection).deleteMany(
                    new Document(ID, new Document("$in", docs.stream().map(d -> d.get(ID)).toList()))));
            throw e;
        }
    }

    private static String key(String aCollection, Object anId) {
        return aCollection + "/" + anId;
    }

    private static String newId() {
        return Ulid.fast().toString().toLowerCase();
    }
}
