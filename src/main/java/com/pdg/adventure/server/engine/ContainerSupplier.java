package com.pdg.adventure.server.engine;

import com.pdg.adventure.api.Container;

import java.util.function.Supplier;

public class ContainerSupplier implements Supplier<Container> {

    private final Supplier<Container> containerResolver;

    // Constructor for eager resolution (when you already have the container)
    public ContainerSupplier(Container aContainer) {
        this.containerResolver = () -> aContainer;
    }

    // Constructor for lazy resolution (when you need to defer container lookup)
    public ContainerSupplier(Supplier<Container> aContainerResolver) {
        this.containerResolver = aContainerResolver;
    }

    @Override
    public Container get() {
        return containerResolver.get();
    }
}
