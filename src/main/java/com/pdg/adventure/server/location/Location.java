package com.pdg.adventure.server.location;

import com.pdg.adventure.api.CommandChain;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Containable;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Direction;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.Visitable;
import com.pdg.adventure.server.engine.Environment;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;
import com.pdg.adventure.server.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.List;

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

        List<CommandChain> availableCommandChains = getCommandChains(aCommandDescription);

        ExecutionResult result = new CommandExecutionResult();
        if (availableCommandChains.isEmpty()) {
            result.setResultMessage("You can't do that.");
        } else if (availableCommandChains.size() > 1) {
            result.setResultMessage(String.format("What do you want to %s?", aCommandDescription.getVerb()));
        } else {
            result = availableCommandChains.get(0).execute();
        }

        if (result.getExecutionState()== ExecutionResult.State.FAILURE) {
            if (Vocabulary.EMPTY_STRING.equals(result.getResultMessage())) {
                result.setResultMessage("You can't do that.");
            }
        } else {
            if (Vocabulary.EMPTY_STRING.equals(result.getResultMessage())) {
                result.setResultMessage("OK.");
            }
        }

        return result;
    }

    private List<CommandChain> getCommandChains(CommandDescription aCommandDescription) {
        List<CommandChain> availableCommands = new ArrayList<>();
        List<CommandChain> chain = getMatchingCommandChain(aCommandDescription);
        availableCommands.addAll(chain);
        chain = container.getMatchingCommandChain(aCommandDescription);
        availableCommands.addAll(chain);
        chain = directions.getMatchingCommandChain(aCommandDescription);
        availableCommands.addAll(chain);
        chain = Environment.getPocket().getMatchingCommandChain(aCommandDescription);
        availableCommands.addAll(chain);
        return availableCommands;
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
