package com.pdg.adventure.server.location;

import java.util.ArrayList;
import java.util.List;

import static com.pdg.adventure.server.parser.CommandExecutor.clarifyExecutionOutcome;

import com.pdg.adventure.api.*;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;
import com.pdg.adventure.server.tangible.Thing;

public class Location extends Thing implements Visitable {

    /** Below this perceived light level, the location is too dark to see anything in it. */
    private static final int MINIMUM_LIGHT_TO_SEE = 10;

    // Matches LocationData's default, so a Location built without an explicit setLight() call
    // (as most hand-built test fixtures are) is lit, not dark.
    private static final int DEFAULT_LUMEN = 50;

    private final Container directions;
    private Container itemContainer;
    // The player's inventory, wired in once when the adventure loads (every Location shares the
    // same instance) so a light-emitting item still counts toward this location's perceived
    // light while it's being carried, not just while it's lying on the floor.
    private Container carriedItems;
    private long timesVisited;

    public Location(DescriptionProvider aDescriptionProvider) {
        super(aDescriptionProvider);
        directions = new GenericContainer(aDescriptionProvider, true, 9999);
        setLight(DEFAULT_LUMEN);
    }

    public Location(DescriptionProvider aDescriptionProvider, Container aPocket) {
        this(aDescriptionProvider);
        itemContainer = aPocket;
    }

    public ExecutionResult addItem(Containable anItem) {
        return itemContainer.add(anItem);
    }

    public ExecutionResult removeItem(Item anItem) {
        return itemContainer.remove(anItem);
    }

    public GenericContainer getItemContainer() {
        return (GenericContainer) itemContainer;
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
            result.setExecutionState(ExecutionResult.State.FAILURE);
            result.setResultMessage(SystemMessageKey.SM8.defaultText());
        } else if (availableCommandChains.size() > 1) {
            result.setExecutionState(ExecutionResult.State.FAILURE);
            result.setResultMessage(SystemMessageKey.SM60.defaultText().formatted(aCommandDescription.getVerb()));
        } else {
            result = availableCommandChains.getFirst().execute();
        }

        return clarifyExecutionOutcome(result);
    }

    @Override
    public List<CommandChain> getMatchingCommandChain(CommandDescription aCommandDescription) {
        List<CommandChain> availableCommands = super.getMatchingCommandChain(aCommandDescription);
        availableCommands.addAll(itemContainer.getMatchingCommandChain(aCommandDescription));
        availableCommands.addAll(directions.getMatchingCommandChain(aCommandDescription));
        return availableCommands;
    }

    /**
     * The description shown when the player arrives at this location: the full (long)
     * description on the very first visit, the short one on every later visit. Exits and
     * visible items are always listed.
     */
    public String getArrivalDescription() {
        String body = timesVisited == 0 ? super.getLongDescription() : getShortDescription();
        return renderDescription(body);
    }

    /**
     * The full description, shown whenever the player explicitly describes or examines this
     * location, regardless of how often it has already been visited.
     */
    @Override
    public String getLongDescription() {
        return renderDescription(super.getLongDescription());
    }

    public void setCarriedItems(Container aCarriedItems) {
        carriedItems = aCarriedItems;
    }

    /**
     * How much light actually reaches this location right now: its own ambient {@code lumen},
     * plus the lumen of every item present - whether lying in the location or carried by the
     * player. A seam for future light sources (a nearby lit location, say) to contribute too,
     * without changing every caller.
     */
    public int getPerceivedLight() {
        return getLight() + sumLumen(itemContainer) + sumLumen(carriedItems);
    }

    private static int sumLumen(Container aContainer) {
        if (aContainer == null) {
            return 0;
        }
        int total = 0;
        for (Containable containable : aContainer.getContents()) {
            if (containable instanceof HasLight lightSource) {
                total += lightSource.getLight();
            }
        }
        return total;
    }

    private boolean isTooDarkToSee() {
        return getPerceivedLight() < MINIMUM_LIGHT_TO_SEE;
    }

    private String renderDescription(String aBody) {
        if (isTooDarkToSee()) {
            return System.lineSeparator() + SystemMessageKey.SM0.defaultText();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append(aBody);

        sb.append(System.lineSeparator());
        if (!directions.isEmpty()) {
            sb.append(SystemMessageKey.SM59.defaultText()).append(System.lineSeparator());
            sb.append(directions.listContents());
        } else {
            sb.append(SystemMessageKey.SM62.defaultText()).append(System.lineSeparator());
        }

        sb.append(System.lineSeparator());
        if (!itemContainer.isEmpty()) {
            sb.append(SystemMessageKey.SM1.defaultText()).append(System.lineSeparator());
            sb.append(itemContainer.listContents());
        }

        return sb.toString();
    }

    public boolean contains(Item anItem) {
        return itemContainer.contains(anItem);
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
               "container=" + itemContainer +
               ", directions=" + directions +
               ", hasBeenVisited=" + timesVisited +
//                ", pocket=" + pocket +
               ", " + super.toString() +
               '}';
    }

    public void setItemContainer(final Container aItemContainer) {
        itemContainer = aItemContainer;
    }
}
