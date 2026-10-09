package com.pdg.adventure.server.storage.service;

import com.github.f4b6a3.ulid.Ulid;
import com.mongodb.DBRef;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The raw-document operations that duplicating, exporting and importing an adventure share. Everything works on
 * {@link Document}s, not on the {@code *Data} classes - see {@link AdventureDuplicator} for why. Not a Spring bean:
 * each service creates one around its {@link MongoTemplate}.
 */
final class AdventureDocumentGraph {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureDocumentGraph.class);

    static final String ADVENTURES = "adventures";
    static final String ID = "_id";
    private static final String DBREF_COLLECTION = "$ref";
    private static final String DBREF_ID = "$id";

    private final MongoTemplate mongoTemplate;

    AdventureDocumentGraph(MongoTemplate aMongoTemplate) {
        mongoTemplate = aMongoTemplate;
    }

    /** @return the raw adventure document, or null if there is none with that id */
    Document findAdventure(String anId) {
        return mongoTemplate.getCollection(ADVENTURES).find(new Document(ID, anId)).first();
    }

    /** Follows every DBRef, transitively, starting at the adventure (which is listed first). */
    Map<String, List<Document>> collectReachableDocuments(Document anAdventure) {
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
                LOG.warn("Dangling reference {} while collecting an adventure", reference);
                continue;
            }
            result.computeIfAbsent(reference.getCollectionName(), _ -> new ArrayList<>()).add(target);
            findReferences(target, pending);
        }
        return result;
    }

    /**
     * Deep copies of all the documents, each with a new id. Every string value and map key that is an old id is
     * replaced by its new id, which rewires all references between the documents.
     */
    static Map<String, List<Document>> copyWithNewIds(Map<String, List<Document>> anOriginals) {
        Map<String, String> idMap = new HashMap<>();
        anOriginals.values().forEach(docs -> docs.forEach(doc -> idMap.put(doc.get(ID).toString(), newId())));

        Map<String, List<Document>> copies = new LinkedHashMap<>();
        anOriginals.forEach((collection, docs) -> {
            List<Document> copied = new ArrayList<>(docs.size());
            docs.forEach(doc -> copied.add((Document) rewrite(doc, idMap)));
            copies.put(collection, copied);
        });
        return copies;
    }

    /** The {@code "collection/id"} of every DBRef in the documents that points at a document not among them. */
    static List<String> findDanglingReferences(Map<String, List<Document>> aDocuments) {
        Set<String> present = new HashSet<>();
        aDocuments.forEach((collection, docs) -> docs.forEach(doc -> present.add(key(collection, doc.get(ID)))));

        Deque<DBRef> references = new ArrayDeque<>();
        aDocuments.values().forEach(docs -> docs.forEach(doc -> findReferences(doc, references)));
        return references.stream()
                         .map(reference -> key(reference.getCollectionName(), reference.getId()))
                         .filter(reference -> !present.contains(reference))
                         .distinct()
                         .toList();
    }

    /** MongoDB cannot span a transaction here (standalone), so undo by hand if a collection fails. */
    void insertAll(Map<String, List<Document>> aCopies) {
        try {
            aCopies.forEach((collection, docs) -> mongoTemplate.getCollection(collection).insertMany(docs));
        } catch (RuntimeException e) {
            // the new ids exist nowhere else, so removing by id cannot touch anything but the partial copy
            aCopies.forEach((collection, docs) -> mongoTemplate.getCollection(collection).deleteMany(
                    new Document(ID, new Document("$in", docs.stream().map(d -> d.get(ID)).toList()))));
            throw e;
        }
    }

    static String newId() {
        return Ulid.fast().toString().toLowerCase();
    }

    private static void findReferences(Object aNode, Deque<DBRef> aPending) {
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
    private static Object rewrite(Object aNode, Map<String, String> anIdMap) {
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

    private static String key(String aCollection, Object anId) {
        return aCollection + "/" + anId;
    }
}
