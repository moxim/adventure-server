package com.pdg.adventure.server.storage.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CascadeSave {
    // using this on a DBRef field will make storing fail
    // as such, this package ist arguably usable

    /*
    java.lang.reflect.InaccessibleObjectException: Unable to make field private static final long java.util.ArrayList.serialVersionUID accessible: module java.base does not "opens java.util" to unnamed module @3f3e6f71
    	at java.base/java.lang.reflect.AccessibleObject.throwInaccessibleObjectException(AccessibleObject.java:387)
    	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:363)
    	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:311)
    	at java.base/java.lang.reflect.Field.checkCanSetAccessible(Field.java:181)
    	at java.base/java.lang.reflect.Field.setAccessible(Field.java:175)
    	at org.springframework.util.ReflectionUtils.makeAccessible(ReflectionUtils.java:779)
    	at com.pdg.adventure.server.storage.mongo.FieldCallback.doWith(FieldCallback.java:12)
     */
}
