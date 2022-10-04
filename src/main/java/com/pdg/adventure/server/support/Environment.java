package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.location.Direction;
import com.pdg.adventure.server.location.Location;

import java.util.List;

public class Environment {
    public static final String EMPTY_STRING = "";

    private Environment() {
        // don't instantiate me
    }

    public static void show(Location aLocation) {
        showDescription(aLocation);
        showDirections(aLocation);
        showContents(aLocation.getContainer());
    }

    private static void showDirections(Location aLocation) {
        final List<Direction> directions = aLocation.getDirections();
        if (directions.size() > 0) {
            tell("Exits are:");
            showShortDescriptions(directions);
        } else {
            tell("There are no obvious exits.");
        }
    }

    private static void showContents(Container aContainer) {
        tell("You also see: ");
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


}
