package com.pdg.adventure.server.storage.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MongoDB event listener that implements cascade delete functionality.
 * When an entity is deleted, this listener automatically deletes all entities
 * referenced in fields annotated with @CascadeDelete.
 *
 * This provides behavior similar to JPA's CascadeType.REMOVE for Spring Data MongoDB.
 *
 * IMPORTANT: This listener uses BeforeDeleteEvent to ensure the entity with all its
 * @DBRef relationships is available for cascade processing before deletion occurs.
 *
 * NOTE: This event-based approach has limitations with Spring Data MongoDB because
 * the event source is converted to a Document before the event fires. For reliable
 * cascade delete, use CascadeDeleteHelper directly in service methods instead.
 *
 * @deprecated Use CascadeDeleteHelper explicitly in service layer for reliable cascade deletes
 */
@Deprecated
// @Component  // Disabled - use CascadeDeleteHelper instead
public class CascadeDeleteMongoEventListener extends AbstractMongoEventListener<Object> {
    private static final Logger LOG = LoggerFactory.getLogger(CascadeDeleteMongoEventListener.class);

    private final MongoTemplate mongoTemplate;
    private final ThreadLocal<Set<Object>> processedObjects = ThreadLocal.withInitial(HashSet::new);
    private final ThreadLocal<Boolean> isProcessing = ThreadLocal.withInitial(() -> false);

    public CascadeDeleteMongoEventListener(MongoTemplate aMongoTemplate) {
        mongoTemplate = aMongoTemplate;
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Object> event) {
        LOG.warn("=== BeforeDeleteEvent fired! Source: {}, Document: {}",
                 event.getSource() != null ? event.getSource().getClass().getSimpleName() : "null",
                 event.getDocument());

        // Avoid recursive processing
        if (isProcessing.get()) {
            LOG.warn("=== Skipping - already processing");
            return;
        }

        try {
            isProcessing.set(true);
            processedObjects.get().clear();
            Object source = event.getSource();

            if (source != null) {
                LOG.warn("=== Processing cascade delete for: {}", source.getClass().getSimpleName());
                cascadeDelete(source);
            } else {
                LOG.error("=== BeforeDeleteEvent source is NULL! Cannot perform cascade delete.");
            }
        } finally {
            processedObjects.remove();
            isProcessing.set(false);
        }
    }

    private void cascadeDelete(Object obj) {
        if (obj == null || processedObjects.get().contains(obj)) {
            LOG.warn("=== Skipping cascadeDelete - null or already processed");
            return;
        }

        processedObjects.get().add(obj);
        Class<?> current = obj.getClass();
        LOG.warn("=== Scanning class {} for @CascadeDelete fields", current.getSimpleName());

        while (current != null && current != Object.class) {
            final Class<?> currentClass = current;  // Make effectively final for lambda
            ReflectionUtils.doWithFields(current, field -> {
                if (field.isAnnotationPresent(CascadeDelete.class)) {
                    LOG.warn("=== Found @CascadeDelete field: {}.{}", currentClass.getSimpleName(), field.getName());
                    ReflectionUtils.makeAccessible(field);
                    Object fieldValue = field.get(obj);

                    if (fieldValue != null) {
                        LOG.warn("=== Field {} has value, processing...", field.getName());
                        deleteReferencedEntities(fieldValue, field);
                    } else {
                        LOG.warn("=== Field {} is null, skipping", field.getName());
                    }
                }
            }, ReflectionUtils.COPYABLE_FIELDS);
            current = current.getSuperclass();
        }
    }

    private void deleteReferencedEntities(Object value, Field field) {
        if (value instanceof Iterable<?> iterable && !(value instanceof Map)) {
            // Handle List/Set: Delete each element
            for (Object item : iterable) {
                if (item != null) {
                    deleteEntity(item);
                }
            }
        } else if (value instanceof Map<?, ?> map) {
            // Handle Map: Delete each value
            for (Object mapValue : map.values()) {
                if (mapValue != null) {
                    deleteEntity(mapValue);
                }
            }
        } else {
            // Handle single object
            deleteEntity(value);
        }
    }

    private void deleteEntity(Object entity) {
        if (entity == null || processedObjects.get().contains(entity)) {
            return;
        }

        LOG.warn("=== Cascade deleting entity: {}", entity.getClass().getSimpleName());

        // First recurse to handle nested cascade deletes
        cascadeDelete(entity);

        // Then delete this entity
        try {
            LOG.warn("=== Calling mongoTemplate.remove() for: {}", entity.getClass().getSimpleName());
            mongoTemplate.remove(entity);
            LOG.warn("=== Successfully deleted: {}", entity.getClass().getSimpleName());
        } catch (Exception e) {
            LOG.error("=== Error deleting entity: {}", entity.getClass().getSimpleName(), e);
        }
    }
}
