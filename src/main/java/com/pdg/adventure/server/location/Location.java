package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Direction;
import com.pdg.adventure.server.api.Visitable;
import com.pdg.adventure.server.engine.ItemIdentifier;
import com.pdg.adventure.server.exception.ItemNotFoundException;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Environment;
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

    public void addDirection(GenericDirection aDirection) {
        directions.add(aDirection);
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

    public boolean applyCommand(CommandDescription aCommandDescription) {
        if (applyCommandInContainer(Environment.getPocket(), aCommandDescription)){
            return true;
        }

        if (commandProvider.hasCommand(aCommandDescription.getDescription()))
        {
            return commandProvider.applyCommand(aCommandDescription);
        }

        for (Direction direction : directions) {
            if (direction.applyCommand(new CommandDescription(aCommandDescription.getVerb()))) {
                return true;
            }
        }

        return applyCommandInContainer(container, aCommandDescription);
    }

    private boolean applyCommandInContainer(Container aContainer, CommandDescription aCommandDescription) {
        try {
            final Containable item = ItemIdentifier.findItem(aContainer, aCommandDescription.getAdjective(),
                    aCommandDescription.getNoun());
            return item.applyCommand(new CommandDescription(aCommandDescription.getVerb()));
        } catch (ItemNotFoundException e) {
            return false;
        }
    }
}
