package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.DefaultContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;

public class Location extends Thing {

    private final Container container;

    public Location(DescriptionProvider aDescriptionProvider) {
        super(aDescriptionProvider);
        container  = new DefaultContainer(aDescriptionProvider, 99);
    }

    public void addItem(Item anItem) {
        container.addItem(anItem);
    }

    public void removeItem(Item anItem) {
        container.removeItem(anItem);
    }
}
