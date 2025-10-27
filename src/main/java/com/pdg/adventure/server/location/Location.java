package com.pdg.adventure.server.location;

import java.util.ArrayList;
import java.util.List;

import static com.pdg.adventure.server.parser.CommandExecutor.clarifyExecutionOutcome;

import com.pdg.adventure.api.*;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;

public class Location extends Thing implements Visitable, HasLight {

    private final Container container;
    private final Container directions;
    private long timesVisited;
    // TODO: reconsider having Location know about the player's pocket
//    private final Container pocket;
    private int lumen;

    public Location(DescriptionProvider aDescriptionProvider, Container aPocket) {
        super(aDescriptionProvider);
        container = new GenericContainer(aDescriptionProvider, 99);
        directions = new GenericContainer(aDescriptionProvider, true, 9999);
//        pocket = aPocket;
        timesVisited = 0; // explicit, but redundant
    }

    public ExecutionResult addItem(Containable anItem) {
        return container.add(anItem);
    }

    public ExecutionResult removeItem(Item anItem) {
        return container.remove(anItem);
    }

    public GenericContainer getContainer() {
        return (GenericContainer) container;
    }

    public ExecutionResult addDirection(Direction aDirection) {
        return directions.add(aDirection);
    }

    public ExecutionResult removeDirection(Direction aDirection) {
        return directions.remove(aDirection);
    }

    @Override
    public long getTimesVisited() {
        return timesVisited;
    }

    @Override
    public void setTimesVisited(long aNumberOfTimesThisHasBeenVisited) {
        timesVisited = aNumberOfTimesThisHasBeenVisited;
    }

    @Override
    public ExecutionResult applyCommand(CommandDescription aCommandDescription) {

        List<CommandChain> availableCommandChains = getMatchingCommandChain(aCommandDescription);

        ExecutionResult result = new CommandExecutionResult();
        if (availableCommandChains.isEmpty()) {
            result.setResultMessage("You can't do that.");
        } else if (availableCommandChains.size() > 1) {
            result.setResultMessage(String.format("What do you want to %s?", aCommandDescription.getVerb()));
        } else {
            result = availableCommandChains.get(0).execute();
        }

        return clarifyExecutionOutcome(result);
    }

    @Override
    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        List<CommandChain> availableCommands = new ArrayList<>(super.getMatchingCommandChain(aCommandDescription));
        availableCommands.addAll(container.getMatchingCommandChain(aCommandDescription));
        availableCommands.addAll(directions.getMatchingCommandChain(aCommandDescription));

        // TODO: move this into interceptor chain
//        chain = pocket.getMatchingCommandChain(aCommandDescription);
//        availableCommands.addAll(chain);

        return availableCommands;
    }

    @Override
    public String getLongDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());

        if (timesVisited == 0) {
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

    public boolean contains(Item anItem) {
        return container.contains(anItem);
    }


    public List<GenericDirection> getDirections() {
        List<GenericDirection> result = new ArrayList<>();
        for (Containable containable : directions.getContents()) {
            result.add((GenericDirection) containable);
        }
        return result;
    }


    @Override
    public String toString() {
        return "Location{" +
                "container=" + container +
                ", directions=" + directions +
                ", hasBeenVisited=" + timesVisited +
//                ", pocket=" + pocket +
                ", " + super.toString() +
                '}';
    }

    @Override
    public void setLight(int aLumenValue) {
        lumen = aLumenValue;
    }

    @Override
    public int getLight() {
        return 0;
    }
}
