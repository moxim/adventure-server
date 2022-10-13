package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.location.Direction;
import com.pdg.adventure.server.location.Location;

import java.util.List;

public class Environment {
    public static final String EMPTY_STRING = "";
    public static Location currentLocation;

    private Environment() {
        // don't instantiate me
    }

    public static void show(Describable aThing) {
        if (aThing instanceof Location) {
            show((Location)aThing);
        } else {
            tell(aThing.getLongDescription());
        }
    }

    public static void show(Location aLocation) {
        tell("");
        showDescription(aLocation);
        showDirections(aLocation);
        showContents(aLocation, "You also see:");
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

    public static void showContents(Location aLocation, String aMessageFormat) {
        Container container = aLocation.getContainer();
        showContents(container, aMessageFormat);
    }

    public static void showContents(Container container, String aMessageFormat) {
        tell(String.format(aMessageFormat, container.getShortDescription()));
        showShortDescriptions(container.getContents());
    }

    private static void showShortDescriptions(List<? extends Describable> items) {
        if (items.isEmpty()) {
            tell("nothing.");
            return;
        }
        for (int i = 0; i < items.size() - 1; i++) {
            tell(describe(items.get(i)) + ", ");
        }
        tell(describe(items.get(items.size() - 1)) + ".");
    }

    private static String describe(Describable anItem) {
        return anItem.getShortDescription();
    }

    private static void showDescription(Location aLocation) {
        if (!aLocation.hasBeenVisited()) {
            tell(aLocation.getLongDescription());
            aLocation.setHasBeenVisited(true);
        } else {
            tell(aLocation.getShortDescription());
        }
    }

    public static void tell(String aMessage) {
        System.out.println(aMessage);
    }

    public static void setCurrentLocation(Location aDestination) {
        currentLocation = aDestination;
        show(aDestination);
    }

    public static Location getCurrentLocation() {
        return currentLocation;
    }
}
