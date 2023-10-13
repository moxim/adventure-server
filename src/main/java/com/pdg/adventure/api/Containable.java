package com.pdg.adventure.api;

public interface Containable extends Actionable {
    boolean isContainable();

    Container getParentContainer();

    void setParentContainer(Container aContainer);
}
