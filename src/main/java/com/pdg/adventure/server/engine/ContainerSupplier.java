package com.pdg.adventure.server.engine;

import com.pdg.adventure.api.Container;

import java.util.function.Supplier;

public class ContainerSupplier implements Supplier<Container> {

    private final Container container;

    public ContainerSupplier(Container aContainer) {
        container = aContainer;
    }

    @Override
    public Container get() {
        return container;
    }
}
