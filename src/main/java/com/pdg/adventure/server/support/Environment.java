package com.pdg.adventure.server.support;

import com.pdg.adventure.server.api.Container;
import com.pdg.adventure.server.api.Describable;
import com.pdg.adventure.server.engine.Workflow;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.parser.CommandDescription;
import com.pdg.adventure.server.tangible.GenericContainer;

public class Environment {
    private static Workflow workflow;
    public static Location currentLocation;
    private static Container pocket;

    private Environment() {
        // don't instantiate me
    }

    public static void show(Describable aThing) {
        if (aThing instanceof Location location) {
            location.setHasBeenVisited(false);
            show(location);
        } else {
            tell(aThing.getLongDescription());
        }
    }

    public static void show(Location aLocation) {
        tell("");
        tell(aLocation.getLongDescription());
    }

    public static void tell(String aMessage) {
        System.out.println(aMessage);
    }

    public static void setCurrentLocation(Location aDestination) {
        currentLocation = aDestination;
    }

    public static Location getCurrentLocation() {
        return currentLocation;
    }

    public static void createPocket() {
        pocket = new GenericContainer(new DescriptionProvider("pocket"), 5);
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

    public static boolean interceptCommands(CommandDescription aCommand) {
        return workflow.interceptCommands(aCommand);
    }
}
