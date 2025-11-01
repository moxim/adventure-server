package com.pdg.adventure.server.storage.mongo;

import java.lang.annotation.*;

/**
 * Annotation to mark fields that should trigger cascade delete operations.
 * When an entity with @CascadeDelete annotated fields is deleted, all referenced entities
 * will be recursively deleted from their respective collections.
 *
 * This is analogous to JPA's CascadeType.REMOVE but for Spring Data MongoDB.
 * Works in conjunction with @DBRef to handle deletion of referenced documents.
 *
 * Example:
 * <pre>
 * &#64;DBRef(lazy = false)
 * &#64;CascadeSave
 * &#64;CascadeDelete
 * private Map&lt;String, LocationData&gt; locationData;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CascadeDelete {}
