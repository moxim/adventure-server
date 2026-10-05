package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.location.Location;

@Component
public class GameContext {

    private Workflow workflow;
    private Location currentLocation;
    private Container pocket;
    private WorkflowData workflowData = new WorkflowData();
    // The console has no fonts: strip the markers a message with its own font carries (see FontMarkup).
    private static final Consumer<String> CONSOLE = line -> IO.println(FontMarkup.strip(line));

    private Consumer<String> outputSink = CONSOLE;
    private String currentPreposition = VocabularyData.EMPTY_STRING;
    private String currentAdverb = VocabularyData.EMPTY_STRING;
    private String currentNoun2 = VocabularyData.EMPTY_STRING;
    private String currentAdjective2 = VocabularyData.EMPTY_STRING;
    private String currentNoun = VocabularyData.EMPTY_STRING;
    private String currentAdjective = VocabularyData.EMPTY_STRING;
    private String currentPictureId;

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
        outputSink = aSink != null ? aSink : CONSOLE;
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

    /**
     * The preposition word (e.g. "on", "off") parsed out of the sub-command currently being
     * dispatched, if any - set fresh by GameLoop before each sub-command so PrepositionCondition
     * can check against it. Never null; VocabularyData.EMPTY_STRING when the input had none.
     */
    public void setCurrentPreposition(String aPreposition) {
        currentPreposition = aPreposition == null ? VocabularyData.EMPTY_STRING : aPreposition;
    }

    public String getCurrentPreposition() {
        return currentPreposition;
    }

    /**
     * The adverb word (e.g. "slowly") parsed out of the sub-command currently being dispatched, if
     * any - set fresh by GameLoop before each sub-command so AdverbCondition can check against it.
     * Never null; VocabularyData.EMPTY_STRING when the input had none.
     */
    public void setCurrentAdverb(String anAdverb) {
        currentAdverb = anAdverb == null ? VocabularyData.EMPTY_STRING : anAdverb;
    }

    public String getCurrentAdverb() {
        return currentAdverb;
    }

    /**
     * The second noun (e.g. "machine" in "use spanner on ancient machine") parsed out of the
     * sub-command currently being dispatched, if any - set fresh by GameLoop before each
     * sub-command so Noun2Condition can check against it. Never null; VocabularyData.EMPTY_STRING
     * when the input had only one noun.
     */
    public void setCurrentNoun2(String aNoun2) {
        currentNoun2 = aNoun2 == null ? VocabularyData.EMPTY_STRING : aNoun2;
    }

    public String getCurrentNoun2() {
        return currentNoun2;
    }

    /**
     * The adjective (e.g. "ancient") describing the second noun of the sub-command currently
     * being dispatched, if any - set fresh by GameLoop before each sub-command so
     * Adjective2Condition can check against it. Never null; VocabularyData.EMPTY_STRING when the
     * input had no second adjective.
     */
    public void setCurrentAdjective2(String anAdjective2) {
        currentAdjective2 = anAdjective2 == null ? VocabularyData.EMPTY_STRING : anAdjective2;
    }

    public String getCurrentAdjective2() {
        return currentAdjective2;
    }

    /**
     * The primary noun of the sub-command currently being dispatched - set fresh by GameLoop
     * before each sub-command so noun-driven actions (AutoTakeAction/AutoDropAction) can see what
     * the player typed. Never null; VocabularyData.EMPTY_STRING when the input had no noun.
     */
    public void setCurrentNoun(String aNoun) {
        currentNoun = aNoun == null ? VocabularyData.EMPTY_STRING : aNoun;
    }

    public String getCurrentNoun() {
        return currentNoun;
    }

    /** The adjective belonging to {@link #getCurrentNoun()}; EMPTY_STRING when none was typed. */
    public void setCurrentAdjective(String anAdjective) {
        currentAdjective = anAdjective == null ? VocabularyData.EMPTY_STRING : anAdjective;
    }

    public String getCurrentAdjective() {
        return currentAdjective;
    }

    public void setCurrentPictureId(String aPictureId) {
        currentPictureId = aPictureId;
    }

    public String getCurrentPictureId() {
        return currentPictureId;
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
