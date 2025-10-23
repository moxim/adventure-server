package com.pdg.adventure.server.storage.mongo;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CascadeSave {}
