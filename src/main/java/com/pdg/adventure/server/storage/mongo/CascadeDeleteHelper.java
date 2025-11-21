package com.pdg.adventure.server.storage.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper utility for performing cascade deletes on entities with @CascadeDelete annotated fields.
 * This is called explicitly from service methods before entity deletion.
 */
@Component
public class CascadeDeleteHelper {
    private static final Logger LOG = LoggerFactory.getLogger(CascadeDeleteHelper.class);

    private final MongoTemplate mongoTemplate;

    public CascadeDeleteHelper(MongoTemplate aMongoTemplate) {
        mongoTemplate = aMongoTemplate;
    }

    /**
     * Performs cascade delete on all @CascadeDelete annotated fields of the given entity.
     * Call this BEFORE deleting the parent entity.
     *
     * @param entity The entity to process for cascade deletes
     */
    public void cascadeDelete(Object entity) {
        if (entity == null) {
            LOG.warn("Cannot cascade delete - entity is null");
            return;
        }

        Set<Object> processedObjects = new HashSet<>();
        LOG.info("Starting cascade delete for: {}", entity.getClass().getSimpleName());
        cascadeDeleteInternal(entity, processedObjects);
    }

    private void cascadeDeleteInternal(Object obj, Set<Object> processedObjects) {
        if (obj == null || processedObjects.contains(obj)) {
            return;
        }

        processedObjects.add(obj);
        Class<?> current = obj.getClass();
        LOG.debug("Scanning class {} for @CascadeDelete fields", current.getSimpleName());

        while (current != null && current != Object.class) {
            final Class<?> currentClass = current;
            ReflectionUtils.doWithFields(current, field -> {
                if (field.isAnnotationPresent(CascadeDelete.class)) {
                    LOG.info("Found @CascadeDelete field: {}.{}", currentClass.getSimpleName(), field.getName());
                    ReflectionUtils.makeAccessible(field);
                    Object fieldValue = field.get(obj);

                    if (fieldValue != null) {
                        LOG.info("Processing @CascadeDelete field: {}", field.getName());
                        deleteReferencedEntities(fieldValue, processedObjects);
                    } else {
                        LOG.debug("Field {} is null, skipping", field.getName());
                    }
                }
            }, ReflectionUtils.COPYABLE_FIELDS);
            current = current.getSuperclass();
        }
    }

    private void deleteReferencedEntities(Object value, Set<Object> processedObjects) {
        if (value instanceof Map<?, ?> map) {
            // Handle Map: Delete each value
            LOG.info("Processing map with {} entries", map.size());
            int count = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object mapValue = entry.getValue();
                if (mapValue != null) {
                    count++;
                    LOG.info("Processing map entry: key={}, valueType={}", entry.getKey(),
                             mapValue.getClass().getSimpleName());
                    deleteEntity(mapValue, processedObjects);
                } else {
                    LOG.warn("Map entry with key={} has null value", entry.getKey());
                }
            }
            LOG.info("Processed {} entries from map", count);
        } else if (value instanceof Iterable<?> iterable) {
            // Handle List/Set: Delete each element
            LOG.info("Processing collection/list with items");
            int count = 0;
            for (Object item : iterable) {
                if (item != null) {
                    count++;
                    deleteEntity(item, processedObjects);
                }
            }
            LOG.info("Processed {} items from collection", count);
        } else {
            // Handle single object
            deleteEntity(value, processedObjects);
        }
    }

    private void deleteEntity(Object entity, Set<Object> processedObjects) {
        if (entity == null || processedObjects.contains(entity)) {
            return;
        }

        LOG.info("Cascade deleting entity: {}", entity.getClass().getSimpleName());

        // First recurse to handle nested cascade deletes
        cascadeDeleteInternal(entity, processedObjects);

        // Then delete this entity
        try {
            mongoTemplate.remove(entity);
            LOG.info("Successfully deleted: {}", entity.getClass().getSimpleName());
        } catch (Exception e) {
            LOG.error("Error deleting entity: {}", entity.getClass().getSimpleName(), e);
        }
    }
}
