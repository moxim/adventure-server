package com.pdg.adventure.server.api;

public interface Containable {
    boolean isContainable();

    Container getParentContainer();
}
