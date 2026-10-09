package com.pdg.adventure.server.storage.service;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pdg.adventure.server.exception.AdventureImportException;

/**
 * Reads a file written by {@link AdventureExporter} into this database as a new, independent adventure.
 * <p>
 * The file is untrusted input. Everything is checked before the first insert - format and version, the collections
 * (an allowlist, so a crafted file cannot write elsewhere), exactly one adventure, an id on every document and
 * no reference to a document the file lacks. Then every document gets a new id (see {@link AdventureDuplicator}),
 * so an import can never overwrite anything and importing a file twice gives two adventures.
 */
@Service
public class AdventureImporter {
    private static final Logger LOG = LoggerFactory.getLogger(AdventureImporter.class);

    /** Every {@code @Document} collection an adventure reaches. Deliberately without saved games. */
    public static final Set<String> ALLOWED_COLLECTIONS = Set.of(
            "adventures", "locations", "containers", "items", "pictures", "vocabularies", "words");

    public static final long DEFAULT_MAX_BYTES = 50L * 1024 * 1024;

    private static final long MEGABYTE = 1024L * 1024;
    private static final String NOT_AN_ADVENTURE_FILE = "This is not an adventure file.";

    /** @param builderVersionDiffers the file was written by another version of the builder than this one */
    public record ImportResult(String adventureId, boolean builderVersionDiffers) {
    }

    private final AdventureDocumentGraph graph;
    private final BuilderVersion builderVersion;
    private final long maxBytes;

    public AdventureImporter(MongoTemplate aMongoTemplate, BuilderVersion aBuilderVersion,
                             @Value("${adventure.import.max-bytes:" + DEFAULT_MAX_BYTES + "}") long aMaxBytes) {
        graph = new AdventureDocumentGraph(aMongoTemplate);
        builderVersion = aBuilderVersion;
        maxBytes = aMaxBytes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    /**
     * @return the id of the new adventure
     * @throws AdventureImportException if the file is not a complete adventure file this builder can read
     */
    public ImportResult importAdventure(byte[] aJson) {
        if (aJson == null || aJson.length == 0) {
            throw new AdventureImportException("The file is empty.");
        }
        if (aJson.length > maxBytes) {
            throw new AdventureImportException("The file is larger than the allowed " + describe(maxBytes) + ".");
        }

        Document envelope = parse(aJson);
        checkHeader(envelope);
        Map<String, List<Document>> documents = readDocuments(envelope);

        List<String> dangling = AdventureDocumentGraph.findDanglingReferences(documents);
        if (!dangling.isEmpty()) {
            throw new AdventureImportException("The file is incomplete: it refers to documents it does not contain ("
                                               + String.join(", ", dangling.stream().limit(5).toList()) + ").");
        }

        String fileVersion = envelope.getString("builderVersion");
        boolean differs = fileVersion != null && builderVersion.current().isPresent()
                          && !fileVersion.equals(builderVersion.current().get());

        Map<String, List<Document>> copies = AdventureDocumentGraph.copyWithNewIds(documents);
        Document adventure = copies.get(AdventureDocumentGraph.ADVENTURES).getFirst();
        Date now = new Date();
        adventure.put("createdAt", now);
        adventure.put("updatedAt", now);

        graph.insertAll(copies);
        String adventureId = adventure.get(AdventureDocumentGraph.ID).toString();
        LOG.info("Imported adventure '{}' as {} ({} documents)", adventure.get("title"), adventureId,
                 copies.values().stream().mapToInt(List::size).sum());
        return new ImportResult(adventureId, differs);
    }

    private static Document parse(byte[] aJson) {
        String text = new String(aJson, StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) {
            text = text.substring(1); // a byte order mark, as some editors write it
        }
        try {
            return Document.parse(text);
        } catch (RuntimeException e) {
            throw new AdventureImportException("The file is not valid JSON.", e);
        }
    }

    private static void checkHeader(Document anEnvelope) {
        if (!AdventureExporter.FORMAT.equals(anEnvelope.get("format"))
            || !(anEnvelope.get("formatVersion") instanceof Number version)) {
            throw new AdventureImportException(NOT_AN_ADVENTURE_FILE);
        }
        if (version.intValue() < 1) {
            throw new AdventureImportException(NOT_AN_ADVENTURE_FILE);
        }
        if (version.intValue() > AdventureExporter.FORMAT_VERSION) {
            throw new AdventureImportException("The file uses format version " + version.intValue()
                                               + ", but this builder reads up to version "
                                               + AdventureExporter.FORMAT_VERSION + ". Please update the builder.");
        }
    }

    private static Map<String, List<Document>> readDocuments(Document anEnvelope) {
        if (!(anEnvelope.get("documents") instanceof Document raw)) {
            throw new AdventureImportException(NOT_AN_ADVENTURE_FILE);
        }
        Map<String, List<Document>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            String collection = entry.getKey();
            if (!ALLOWED_COLLECTIONS.contains(collection)) {
                throw new AdventureImportException("The file contains data for '" + collection
                                                   + "', which adventures do not use.");
            }
            if (!(entry.getValue() instanceof List<?> list)) {
                throw new AdventureImportException(NOT_AN_ADVENTURE_FILE);
            }
            Set<String> ids = new HashSet<>();
            List<Document> docs = new ArrayList<>(list.size());
            for (Object element : list) {
                if (!(element instanceof Document doc) || !(doc.get(AdventureDocumentGraph.ID) instanceof String id)
                    || id.isBlank() || !ids.add(id)) {
                    throw new AdventureImportException("The file contains a damaged or duplicated document in '"
                                                       + collection + "'.");
                }
                docs.add(doc);
            }
            result.put(collection, docs);
        }
        if (result.getOrDefault(AdventureDocumentGraph.ADVENTURES, List.of()).size() != 1) {
            throw new AdventureImportException("The file must contain exactly one adventure.");
        }
        return result;
    }

    private static String describe(long aByteCount) {
        return aByteCount >= MEGABYTE ? aByteCount / MEGABYTE + " MB" : aByteCount + " bytes";
    }
}
