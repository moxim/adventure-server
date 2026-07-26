package com.pdg.adventure.server.support;

import java.util.Objects;

public record Variable(String name, Integer value) {
    public Variable {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
    }
}
