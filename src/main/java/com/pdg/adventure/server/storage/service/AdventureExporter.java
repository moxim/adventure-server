package com.pdg.adventure.server.storage.service;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Writes an adventure - the adventure document plus every document it reaches through a {@code @DBRef} - as one
 * JSON file that {@link AdventureImporter} can read into another database.
 * <p>
 * The file is an envelope around canonical Extended JSON documents, so dates, picture bytes and DBRefs keep their
 * types. Like {@link AdventureDuplicator} it works on the raw documents and therefore needs no knowledge of the
 * {@code *Data} classes. Bump {@link #FORMAT_VERSION} when the envelope itself changes incompatibly.
 */
@Service
public class AdventureExporter {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureExporter.class);

    public static final String FORMAT = "adventurebuilder-adventure";
    public static final int FORMAT_VERSION = 1;

    private static final JsonWriterSettings JSON_SETTINGS =
            JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).indent(true).build();

    private final MongoTemplate mongoTemplate;
    private final AdventureDocumentGraph graph;
    private final BuilderVersion builderVersion;

    public AdventureExporter(MongoTemplate aMongoTemplate, BuilderVersion aBuilderVersion) {
        mongoTemplate = aMongoTemplate;
        graph = new AdventureDocumentGraph(aMongoTemplate);
        builderVersion = aBuilderVersion;
    }

    /**
     * @return the UTF-8 JSON file content
     * @throws IllegalArgumentException if there is no adventure with the given id
     * @throws IllegalStateException    if the adventure refers to documents that no longer exist (the file could
     *                                  not be imported again)
     */
    public byte[] export(String anAdventureId) {
        Document source = graph.findAdventure(anAdventureId);
        if (source == null) {
            throw new IllegalArgumentException("Adventure not found: " + anAdventureId);
        }

        Map<String, List<Document>> reachable = graph.collectReachableDocuments(source);
        List<String> dangling = AdventureDocumentGraph.findDanglingReferences(reachable);
        if (!dangling.isEmpty()) {
            // TODO: Review needed — refusing keeps an un-importable file from being written; alternative: export anyway and let the import reject it
            throw new IllegalStateException("Adventure " + anAdventureId
                                            + " refers to documents that no longer exist: " + dangling);
        }

        Document documents = new Document();
        reachable.forEach(documents::put);
        Document envelope = new Document("format", FORMAT)
                .append("formatVersion", FORMAT_VERSION)
                .append("builderVersion", builderVersion.current().orElse(null))
                .append("exportedAt", Instant.now().toString())
                .append("documents", documents);

        // the database's registry knows how to write a DBRef; Document's built-in default does not
        Codec<Document> codec = mongoTemplate.getDb().getCodecRegistry().get(Document.class);
        byte[] json = envelope.toJson(JSON_SETTINGS, codec).getBytes(StandardCharsets.UTF_8);
        LOG.info("Exported adventure {} ({} documents, {} bytes)", anAdventureId,
                 reachable.values().stream().mapToInt(List::size).sum(), json.length);
        return json;
    }
}
