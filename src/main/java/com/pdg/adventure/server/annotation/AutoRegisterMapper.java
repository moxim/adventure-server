package com.pdg.adventure.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Alternative annotation that automatically detects mapper types from the generic interface.
 * This is more convenient as you don't need to specify the classes manually.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterMapper {
    
    /**
     * Optional priority for registration order (lower numbers = higher priority)
     * Useful when mappers have dependencies on each other
     */
    int priority() default 100;
    
    /**
     * Optional description for documentation purposes
     */
    String description() default "";
}