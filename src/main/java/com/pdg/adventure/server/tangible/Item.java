package com.pdg.adventure.server.tangible;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.support.DescriptionProvider;

public class Item extends Thing implements Containable {
    private final boolean isContainable;
    private Container parentContainer;

    public Item(DescriptionProvider aDescriptionProvider, boolean isContainable) {
        super(aDescriptionProvider);
        this.isContainable = isContainable;
    }

    @Override
    public boolean isContainable() {
        return isContainable;
    }

    @Override
    public Container getParentContainer() {
        return parentContainer;
    }

    public void setParentContainer(Container aParentContainer) {
        parentContainer = aParentContainer;
    }
}
