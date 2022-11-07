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

public class Location extends Thing implements Visitable {

    private final Container container;
    private final Container directions;
    private boolean hasBeenVisited;

    public Location(DescriptionProvider aDescriptionProvider) {
        super(aDescriptionProvider);
        container = new GenericContainer(aDescriptionProvider, 99);
        directions = new GenericContainer(aDescriptionProvider, true, 99);
        hasBeenVisited = false; // explicit, but redundant
    }

    public boolean addItem(Item anItem) {
        return container.add(anItem);
    }

    public boolean removeItem(Item anItem) {
        return container.remove(anItem);
    }

    public Container getContainer() {
        return container;
    }

    public boolean addDirection(Direction aDirection) {
        return directions.add(aDirection);
    }

    public boolean removeDirection(Direction aDirection) {
        return directions.remove(aDirection);
    }

    @Override
    public boolean hasBeenVisited() {
        return hasBeenVisited;
    }

    @Override
    public void setHasBeenVisited(boolean aFlagWhetherThisHasBeenSeen) {
        hasBeenVisited = aFlagWhetherThisHasBeenSeen;
    }

    @Override
    public boolean applyCommand(CommandDescription aCommandDescription) {
        // TODO:
        //  bring these returns into one ExecutionSateType
        if (commandProvider.applyCommand(aCommandDescription)) {
            return true;
        }

       if (applyCommandInContainer(container, aCommandDescription)) {
           return true;
       }

        if (applyCommandInContainer(directions, aCommandDescription)) {
            return true;
        }

        if (applyCommandInContainer(Environment.getPocket(), aCommandDescription)) {
            return true;
        }

        return false;
    }

    private boolean applyCommandInContainer(Container aContainer, CommandDescription aCommandDescription) {
        try {
            final Containable item = ItemIdentifier.findItem(aContainer, aCommandDescription);
            return item.applyCommand(aCommandDescription);
        } catch (ItemNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getLongDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty(Environment.LF));

        if (!hasBeenVisited()) {
            sb.append(super.getLongDescription());
        } else {
            sb.append(getShortDescription());
        }

        sb.append(System.getProperty(Environment.LF));
        if (!directions.isEmpty()) {
            sb.append("Exits are:").append(System.getProperty(Environment.LF));
            sb.append(directions.listContents());
        } else {
            sb.append("There are no obvious exits.");
        }

        sb.append(System.getProperty(Environment.LF));
        if (!container.isEmpty()) {
            sb.append("You also see:").append(System.getProperty(Environment.LF));
            sb.append(container.listContents());
        }

        return sb.toString();
    }

    public boolean contains(Containable anItem) {
        return container.contains(anItem);
    }
}
