package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Component;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.location.Location;

@Component
public class GameContext {

    private Workflow workflow;
    private Location currentLocation;
    private Container pocket;

    public void show(Describable aThing) {
        tell(aThing.getLongDescription());
    }

    public void tell(String aMessage) {
        System.out.println(aMessage);
    }

    public void setCurrentLocation(Location aDestination) {
        currentLocation = aDestination;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setPocket(Container aContainer) {
        pocket = aContainer;
    }

    public Container getPocket() {
        return pocket;
    }

    public Workflow setUpWorkflows() {
        workflow = new Workflow(this);
        return workflow;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void preProcessCommands() {
        workflow.preProcess();
    }

    public ExecutionResult interceptCommands(CommandDescription aCommand) {
        return workflow.interceptCommands(aCommand);
    }
}
