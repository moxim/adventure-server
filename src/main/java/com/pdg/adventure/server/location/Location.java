package com.pdg.adventure.server.location;

import com.pdg.adventure.server.api.*;
import com.pdg.adventure.server.engine.ItemIdentifier;
import com.pdg.adventure.server.exception.AmbiguousCommandException;
import com.pdg.adventure.server.exception.ItemNotFoundException;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.engine.Environment;
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

    public ExecutionResult addItem(Item anItem) {
        return container.add(anItem);
    }

    public ExecutionResult removeItem(Item anItem) {
        return container.remove(anItem);
    }

    public Container getContainer() {
        return container;
    }

    public ExecutionResult addDirection(Direction aDirection) {
        return directions.add(aDirection);
    }

    public ExecutionResult removeDirection(Direction aDirection) {
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
    public ExecutionResult applyCommand(CommandDescription aCommandDescription) {
        // TODO:
        //  bring these returns into one ExecutionSateType
        ExecutionResult result = commandProvider.applyCommand(aCommandDescription);
        if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
            return result;
        }

        if (!result.hasCommandMatched()) {
            result = applyCommandInContainer(container, aCommandDescription);
            if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
                return result;
            }
        }

        if (!result.hasCommandMatched()) {
            result = applyCommandInContainer(directions, aCommandDescription);
            if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
                return result;
            }
        }

        if (!result.hasCommandMatched()) {
            result = applyCommandInContainer(Environment.getPocket(), aCommandDescription);
            if (result.getExecutionState() == ExecutionResult.State.SUCCESS) {
                return result;
            }
        }

        result.setResultMessage("You can't do that.");
        return result;
    }

    private ExecutionResult applyCommandInContainer(Container aContainer, CommandDescription aCommandDescription) {
        ExecutionResult result = new CommandExecutionResult();
        try {
            final Containable item = ItemIdentifier.findItem(aContainer, aCommandDescription);
            result = item.applyCommand(aCommandDescription);
        } catch (AmbiguousCommandException e) {
            result.setResultMessage(e.getMessage());
        } catch (ItemNotFoundException e) {
            result.setResultMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public String getLongDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());

        if (!hasBeenVisited()) {
            sb.append(super.getLongDescription());
        } else {
            sb.append(getShortDescription());
        }

        sb.append(System.lineSeparator());
        if (!directions.isEmpty()) {
            sb.append("Exits are:").append(System.lineSeparator());
            sb.append(directions.listContents());
        } else {
            sb.append("There are no obvious exits.");
        }

        sb.append(System.lineSeparator());
        if (!container.isEmpty()) {
            sb.append("You also see:").append(System.lineSeparator());
            sb.append(container.listContents());
        }

        return sb.toString();
    }

    public boolean contains(Containable anItem) {
        return container.contains(anItem);
    }
}
