package com.pdg.adventure.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark mapper classes for automatic registration.
 * The mapper will be automatically registered with the MapperSupporter
 * during application startup.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterMapper {
    
    /**
     * The data object (DTO) class that this mapper handles
     */
    Class<?> dataObjectClass();
    
    /**
     * The business object class that this mapper handles
     */
    Class<?> businessObjectClass();
    
    /**
     * Optional priority for registration order (lower numbers = higher priority)
     * Useful when mappers have dependencies on each other
     */
    int priority() default 100;
}