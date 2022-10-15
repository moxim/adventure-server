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

    public boolean applyCommand(CommandDescription aCommand) {
        final String verb = aCommand.getVerb();
        final String adjective = aCommand.getAdjective();
        final String noun = aCommand.getNoun();

        if (applyCommandInContainer(Environment.getPocket(), verb, adjective, noun)){
            return true;
        };

        if (commandProvider.hasCommand(verb) &&
                (noun.isEmpty() || noun.equals(getNoun())) &&
                (adjective.isEmpty() || adjective.equals(getAdjective())))
        {
            return commandProvider.applyCommand(verb);
        }

        for (Direction direction : directions) {
            if (direction.applyCommand(aCommand)) {
                return true;
            }
//            if (direction.getDestination().applyCommand(aCommand)) {
//                return true;
//            }
        }

        return applyCommandInContainer(container, verb, adjective, noun);
    }

    private boolean applyCommandInContainer(Container aContainer, String verb, String adjective, String noun) {
        try {
            final Containable item = ItemIdentifier.findItem(aContainer, adjective, noun);
            return item.applyCommand(verb);
        } catch (ItemNotFoundException e) {
            return false;
        }
    }
}
