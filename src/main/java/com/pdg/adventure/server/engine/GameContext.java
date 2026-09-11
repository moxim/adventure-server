package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.location.Location;

@Component
public class GameContext {

    private Workflow workflow;
    private Location currentLocation;
    private Container pocket;
    private WorkflowData workflowData = new WorkflowData();
    private Consumer<String> outputSink = IO::println;

    public void show(Describable aThing) {
        tell(aThing.getLongDescription());
    }

    public void tell(String aMessage) {
        outputSink.accept(aMessage);
    }

    /**
     * Redirects tell() output, e.g. so a browser Test session can capture gameplay text
     * instead of it going to the console. GameContext is a process-wide singleton, so callers
     * must install the sink immediately before driving the engine and clear it (pass null)
     * right after — never leave a non-default sink installed between calls.
     */
    public void setOutputSink(Consumer<String> aSink) {
        outputSink = aSink != null ? aSink : IO::println;
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

    public void setWorkflowData(WorkflowData aWorkflowData) {
        workflowData = aWorkflowData;
    }

    public WorkflowData getWorkflowData() {
        return workflowData;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public ExecutionResult runProcesses() {
        return workflow.runProcesses();
    }

    public ExecutionResult runArrivalProcesses() {
        return workflow.runArrivalProcesses();
    }

    public ExecutionResult respondTo(CommandDescription aCommand) {
        return workflow.respondTo(aCommand);
    }
}
