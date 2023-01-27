package com.pdg.adventure.server.engine;

import java.util.function.Supplier;

import com.pdg.adventure.api.Container;

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
