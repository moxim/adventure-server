package com.pdg.adventure.server.support;

import java.util.Objects;

public record Variable(String aName, String aValue) {
    public Variable {
        Objects.requireNonNull(aName);
        Objects.requireNonNull(aValue);
    }
}
