package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Visitable;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;

import java.util.ArrayList;
import java.util.List;

public class Location extends Thing implements Visitable {

    private final Container container;
    private final List<Direction> directions;
    private boolean hasBeenVisited;

    public Location(DescriptionProvider aDescriptionProvider) {
        super(aDescriptionProvider);
        container = new GenericContainer(aDescriptionProvider, 99);
        directions = new ArrayList<>();
        hasBeenVisited = false; // explicit, but redundant
    }

    public void add(Item anItem) {
        container.add(anItem);
    }

    public void remove(Item anItem) {
        container.remove(anItem);
    }

    public Container getContainer() {
        return container;
    }

    public void addDirection(Direction aDirection) {
        directions.add(aDirection);
        getCommandProvider().addCommand(aDirection);
    }

    public List<Direction> getDirections() {
        return directions;
    }

    @Override
    public boolean hasBeenVisited() {
        return hasBeenVisited;
    }

    @Override
    public void setHasBeenVisited(boolean aFlagWhetherThisHasBeenSeen) {
        hasBeenVisited = aFlagWhetherThisHasBeenSeen;
    }

}
