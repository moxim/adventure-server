package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.Containable;
import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Visitable;
import com.pdg.adventure.server.engine.ItemIdentifier;
import com.pdg.adventure.server.exception.ItemNotFoundException;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.Environment;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;

import java.util.List;

public class Location extends Thing implements Visitable {

    private final Container container;
    private final Container directions;
    private boolean hasBeenVisited;

    public Location(DescriptionProvider aDescriptionProvider) {
        super(aDescriptionProvider);
        container = new GenericContainer(aDescriptionProvider, 99);
        directions = new GenericContainer(aDescriptionProvider, 99);
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

    public List<Containable> getDirections() {
        return container.getContents();
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

        for (Containable direction : directions.getContents()) {
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

    public String getLongDescription() {
        StringBuilder sb = new StringBuilder();

        if (!hasBeenVisited()) {
            sb.append(super.getLongDescription());
        } else {
            sb.append(getShortDescription());
        }

        sb.append(System.getProperty("line.separator"));
        if (!directions.isEmpty()) {
            sb.append("Exits are:").append(System.getProperty("line.separator"));
            sb.append(directions.listContents());
        } else {
            sb.append("There are no obvious exits.");
        }

        sb.append(System.getProperty("line.separator"));
        if (!container.isEmpty()) {
            sb.append("You also see:").append(System.getProperty("line.separator"));
            sb.append(container.listContents());
        }

        return sb.toString();
    }
}
