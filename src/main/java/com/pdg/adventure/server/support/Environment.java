package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.location.Direction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.tangible.Thing;

import java.util.List;

public class Environment {
    public static final String EMPTY_STRING = "";

    private static final Thing player = new Thing(new DescriptionProvider("you"));
    private static final VariableProvider variableProvider = new VariableProvider();

    private Environment() {
        // don't instantiate me
    }

    public static Thing getPlayer() {
        return player;
    }

    public static void show(Location aLocation) {
        showDescription(aLocation);
        showDirections(aLocation);
        showContents(aLocation.getContainer(), "You also see:");
    }

    private static void showDirections(Location aLocation) {
        final List<Direction> directions = aLocation.getDirections();
        if (!directions.isEmpty()) {
            tell("Exits are:");
            showShortDescriptions(directions);
        } else {
            tell("There are no obvious exits.");
        }
    }

    public static void showContents(Container aContainer, String aMessageForamt) {
        tell(String.format(aMessageForamt, aContainer.getShortDescription()));
        showShortDescriptions(aContainer.getContents());
    }

    private static void showShortDescriptions(List<? extends Describable> items) {
        if (items.isEmpty()) {
            return;
        }
        for (int i = 0; i < items.size() - 1; i++) {
            tell(describe(items.get(i)) + ", ");
        }
        tell(describe(items.get(items.size() - 1)));
    }

    private static String describe(Describable anItem) {
        return "a " + anItem.getShortDescription();
    }

    private static void showDescription(Location aLocation) {
        if (!aLocation.hasBeenVisited()) {
            tell(aLocation.getLongDescription() + ".");
            aLocation.setHasBeenVisited(true);
        } else {
            tell("The " + aLocation.getShortDescription() + ".");
        }
    }

    public static void tell(String aMessage) {
        System.out.println(aMessage);
    }

    public static Variable getVariable(String aName) {
        return variableProvider.get(aName);
    }

    public static void setVariable(String aName, String aValue) {
        variableProvider.set(new Variable(aName, aValue));
    }
}
