package com.pdg.adventure.server.api;

public interface Containable extends Describable {
    boolean isContainable();

    Container getParentContainer();

    String getShortDescription();

    void setParentContainer(Container aContainer);
}
