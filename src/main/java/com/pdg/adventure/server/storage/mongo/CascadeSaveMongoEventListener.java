package com.pdg.adventure.server.storage.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pdg.adventure.api.Ided;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)  // Run after other listeners (like UuidIdGenerationMongoEventListener)
public class CascadeSaveMongoEventListener extends AbstractMongoEventListener<Object> {
    private final ThreadLocal<Set<Object>> processedObjects = ThreadLocal.withInitial(HashSet::new);
            // Per-thread to avoid cycles
    private final ThreadLocal<Integer> eventDepth = ThreadLocal.withInitial(() -> 0);

    private static final Logger LOG = LoggerFactory.getLogger(CascadeSaveMongoEventListener.class);

    private final MongoTemplate mongoTemplate;
    private final UuidIdGenerationMongoEventListener uuidIdGenerationMongoEventListener;

    public CascadeSaveMongoEventListener(final MongoTemplate aMongoTemplate,
                                         final UuidIdGenerationMongoEventListener aUuidIdGenerationMongoEventListener) {
        mongoTemplate = aMongoTemplate;
        uuidIdGenerationMongoEventListener = aUuidIdGenerationMongoEventListener;
    }

    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        int depth = eventDepth.get();
        if (depth == 0) {
            processedObjects.get().clear();
        }
        eventDepth.set(++depth);

        Object source = event.getSource();
        if (source != null && !processedObjects.get().contains(source)) {
            processedObjects.get().add(source);
            assignUuidsRecursively(source); // Assign UUIDs to all nested Ided entities first
            recurseAndCascade(source);      // Then perform cascade save
        }

        eventDepth.set(eventDepth.get() - 1);
        if (eventDepth.get() == 0) {
            processedObjects.remove();
            eventDepth.remove();
        }
    }

    private void assignUuidsRecursively(Object obj) {
        Class<?> current = obj.getClass();
        while (current != null && current != Object.class) {
            ReflectionUtils.doWithFields(current, field -> {
                ReflectionUtils.makeAccessible(field);
                Object fieldValue = field.get(obj);

                if (fieldValue == null) return;

                if (fieldValue instanceof Ided) {
                    uuidIdGenerationMongoEventListener.onBeforeConvert(
                        new BeforeConvertEvent<>((Ided) fieldValue, null)
                    );
                }

                if (field.isAnnotationPresent(CascadeSave.class)) {
                    if (fieldValue instanceof Iterable<?> iterable && !(fieldValue instanceof Map)) {
                        for (Object item : iterable) {
                            if (item != null) assignUuidsRecursively(item);
                        }
                    } else if (fieldValue instanceof Map<?, ?> map) {
                        for (Object mapValue : map.values()) {
                            if (mapValue != null) assignUuidsRecursively(mapValue);
                        }
                    } else {
                        assignUuidsRecursively(fieldValue);
                    }
                } else if (isEmbeddedObject(field, fieldValue)) {
                    assignUuidsRecursively(fieldValue);
                }
            }, ReflectionUtils.COPYABLE_FIELDS);
            current = current.getSuperclass();
        }
    }

    private void recurseAndCascade(Object obj) {
        Class<?> current = obj.getClass();
        LOG.debug(">>>>Processing object of class: {}", current.getName());
        while (current != null && current != Object.class) {
            LOG.debug("Inspecting class: {}", current.getName());
            ReflectionUtils.doWithFields(current, field -> {
                final String fieldName = field.getName();
                LOG.debug("Working on field: {}", fieldName);

                ReflectionUtils.makeAccessible(field);
                Object fieldValue = field.get(obj);

                if (fieldValue == null && !fieldName.equals("id")) {
                    LOG.debug("  Field {} is null, skipping.", fieldName);
                    return;
                }

                if (field.isAnnotationPresent(CascadeSave.class)) {
                    // Cascade save for @DBRef fields (single or collection/map)
                    cascadeSave(fieldValue);
                } else if (fieldValue != null && isEmbeddedObject(field, fieldValue)) {
                    // Recurse into embedded objects (non-@DBRef complex fields like DescriptionData)
                    if (!processedObjects.get().contains(fieldValue)) {
                        processedObjects.get().add(fieldValue);
                        recurseAndCascade(fieldValue);
                    }
                }
            }, ReflectionUtils.COPYABLE_FIELDS);
            current = current.getSuperclass();
        }
        LOG.debug("<<<<Done with object of class: {}", obj.getClass().getName());
    }

    private void cascadeSave(Object value) {
        if (processedObjects.get().contains(value)) {
            LOG.debug("Have already considered {}", value);
            return;  // Avoid cycles
        }
        LOG.debug("Processing {} ", value);
        processedObjects.get().add(value);

        // Manually trigger UUID assignment
        if (value instanceof Ided) {
            uuidIdGenerationMongoEventListener.onBeforeConvert(
                    new BeforeConvertEvent<>((Ided) value, null)
            );
        }

        if (value instanceof Iterable<?> iterable && !(value instanceof Map)) {
            // Handle List/Set: Save each element
            for (Object item : iterable) {
                if (item != null) {
                    if (item instanceof Ided) {
                        uuidIdGenerationMongoEventListener.onBeforeConvert(
                            new BeforeConvertEvent<>((Ided) item, null)
                        );
                    }
                    mongoTemplate.save(item);
                    recurseAndCascade(item);  // Recurse into saved item if needed
                }
            }
        } else if (value instanceof Map<?, ?> map) {
            // Handle Map: Save each value
            for (Object mapValue : map.values()) {
                if (mapValue != null) {
                    if (mapValue instanceof Ided) {
                        uuidIdGenerationMongoEventListener.onBeforeConvert(
                            new BeforeConvertEvent<>((Ided) mapValue, null)
                        );
                    }
                    mongoTemplate.save(mapValue);
                    recurseAndCascade(mapValue);
                }
            }
        } else {
            // Handle single object
            mongoTemplate.save(value);
            recurseAndCascade(value);  // Recurse after save
        }

        if (value instanceof Ided) {
            LOG.debug("Value: ", value);
        }
    }

    private boolean isEmbeddedObject(Field field, Object value) {
        // Embedded if: not @DBRef, value is a non-primitive object (not String, Number, etc.), and not a collection/map
        return !field.isAnnotationPresent(DBRef.class)
                && !value.getClass().isPrimitive()
                && !String.class.isAssignableFrom(value.getClass())
                && !Number.class.isAssignableFrom(value.getClass())
                && !Boolean.class.isAssignableFrom(value.getClass())
                && !Enum.class.isAssignableFrom(value.getClass())
                && !(value instanceof Iterable<?>)
                && !(value instanceof Map);
    }
}
