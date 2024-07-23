package com.pdg.adventure.server.engine;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.location.Location;

public class Environment {

    private static Workflow workflow;
    private static Location currentLocation;
    private static Container pocket;

    private Environment() {
        // don't instantiate me
    }

    public static void show(Describable aThing) {
        tell(aThing.getLongDescription());
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

    public static void setPocket(Container aContainer) {
        pocket = aContainer;
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

    public static ExecutionResult interceptCommands(CommandDescription aCommand) {
        ExecutionResult result = workflow.interceptCommands(aCommand);
        return result;
    }
}
