package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.api.Direction;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.tangible.GenericContainer;

import java.util.List;

public class Environment {
    private static Workflow workflow;
    public static Location currentLocation;
    private static Container pocket;

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


    public static void createPocket() {
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 3);
    }

    public static Container getPocket() {
        return pocket;
    }

    public static void setUpWorkflows() {
        workflow = new Workflow();
    }

    public static Workflow getWorkflow() {
        return workflow;
    }

    public static void preProcessCommands() {
        workflow.preProcess();
    }

    public static void postProcessCommands() {
        workflow.postProcess();
    }

    public static boolean alwaysProcessCommands(CommandDescription aCommand) {
        return workflow.alwaysProcess(aCommand);
    }
}
