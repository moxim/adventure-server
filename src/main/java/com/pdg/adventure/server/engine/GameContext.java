package com.pdg.adventure.server.engine;

import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.Describable;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.server.location.Location;

@Component
@PerBrowserSession
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
    private String currentVerb = VocabularyData.EMPTY_STRING;
    private String currentNoun = VocabularyData.EMPTY_STRING;
    private String currentAdjective = VocabularyData.EMPTY_STRING;
    private String currentPictureId;
    private PictureMode pictureMode = PictureMode.DEFAULT;
    // The grid cell of the click GameLoop is running the Processes for; null outside such a run.
    private Integer clickedColumn;
    private Integer clickedRow;
    private boolean clickHandled;

    /** Who is playing which adventure, and the builder version the adventure carried; set when a run starts. */
    public record RunIdentity(String playerId, String adventureId, String adventureTitle, String builderVersion) {
    }

    private RunIdentity runIdentity;

    public void show(Describable aThing) {
        tell(aThing.getLongDescription());
    }

    public void tell(String aMessage) {
        outputSink.accept(aMessage);
    }

    /**
     * Redirects tell() output, e.g. so a browser Test session can capture gameplay text
     * instead of it going to the console. GameContext is scoped to the browser session, so callers
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
     * Whether the player would notice a thing being added to or removed from the container, so that actions like
     * Create and Destroy show their message only to a player who is there to see it. That is the case for the player's
     * pocket (it is felt even in the dark) and for the item container of the current location, unless that is too dark
     * to see anything. What happens inside any other container, a chest in the room included, goes unnoticed.
     * <p>
     * The location and the pocket are looked at on every call: the player moves during a game.
     *
     * @param aContainer where a thing is added or removed; null (a thing without a container) is not noticed
     */
    public boolean playerNoticesChangeIn(Container aContainer) {
        if (aContainer == null) {
            return false;
        }
        if (aContainer.equals(pocket)) {
            return true;
        }
        return currentLocation != null && !currentLocation.isTooDarkToSee()
               && aContainer.equals(currentLocation.getItemContainer());
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
     * The verb of the sub-command currently being dispatched (the word the vocabulary resolves the typed one to) - set
     * fresh by GameLoop before each sub-command so an action can name it in a message ("Which suit should I take?").
     * Never null; VocabularyData.EMPTY_STRING when there is none.
     */
    public void setCurrentVerb(String aVerb) {
        currentVerb = aVerb == null ? VocabularyData.EMPTY_STRING : aVerb;
    }

    public String getCurrentVerb() {
        return currentVerb;
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

    /**
     * Starts a click: records the cell of the picture grid (column, row, counted from 0) and empties the typed
     * fields, so that what the player typed last cannot satisfy a condition during the click run. Ended by
     * {@link #endClick()}.
     */
    public void beginClick(int aColumn, int aRow) {
        clickedColumn = aColumn;
        clickedRow = aRow;
        clickHandled = false;
        setCurrentVerb(null);
        setCurrentAdjective(null);
        setCurrentNoun(null);
        setCurrentPreposition(null);
        setCurrentAdverb(null);
        setCurrentNoun2(null);
        setCurrentAdjective2(null);
    }

    public void endClick() {
        clickedColumn = null;
        clickedRow = null;
        clickHandled = false;
    }

    /** Whether a click is being run and it is in this cell; false for a missing coordinate. */
    public boolean isClickAt(Integer aColumn, Integer aRow) {
        return clickedColumn != null && clickedColumn.equals(aColumn) && clickedRow != null && clickedRow.equals(aRow);
    }

    /** Called when a command with a CLICKED condition for the click ran its actions: the click was understood. */
    public void markClickHandled() {
        clickHandled = true;
    }

    public boolean isClickHandled() {
        return clickHandled;
    }

    /** Whether a location's picture is shown on arrival; DEFAULT until a PictureMode action says otherwise. */
    public PictureMode getPictureMode() {
        return pictureMode;
    }

    public void setPictureMode(PictureMode aPictureMode) {
        pictureMode = aPictureMode == null ? PictureMode.DEFAULT : aPictureMode;
    }

    public void setCurrentPictureId(String aPictureId) {
        currentPictureId = aPictureId;
    }

    public String getCurrentPictureId() {
        return currentPictureId;
    }

    public RunIdentity getRunIdentity() {
        return runIdentity;
    }

    public void setRunIdentity(RunIdentity aRunIdentity) {
        runIdentity = aRunIdentity;
    }

    public Workflow setUpWorkflows() {
        workflow = new Workflow();
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
