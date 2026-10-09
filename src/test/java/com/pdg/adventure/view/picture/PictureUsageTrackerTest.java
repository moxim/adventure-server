package com.pdg.adventure.view.picture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;

class PictureUsageTrackerTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setLocationData(new HashMap<>());
    }

    @Test
    void findPictureUsages_returnsEmpty_whenAdventureDataIsNull() {
        assertThat(PictureUsageTracker.findPictureUsages(null, "pic-1")).isEmpty();
    }

    @Test
    void findPictureUsages_returnsEmpty_whenPictureIdIsNull() {
        assertThat(PictureUsageTracker.findPictureUsages(adventureData, null)).isEmpty();
    }

    @Test
    void findPictureUsages_findsLocationsDefaultPicture() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setPictureId("pic-1");
        adventureData.getLocationData().put(location.getId(), location);

        List<PictureUsageTracker.PictureUsage> usages =
                PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).hasSize(1);
        assertThat(usages.getFirst().getDisplayText()).contains("Location Default Picture");
    }

    @Test
    void findPictureUsages_findsTheWorldMap() {
        adventureData.setWorldMapPictureId("pic-1");

        List<PictureUsageTracker.PictureUsage> usages =
                PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).hasSize(1);
        assertThat(usages.getFirst().getDisplayText()).isEqualTo("World Map: is the world map of this adventure");
        assertThat(PictureUsageTracker.isPictureUsed(adventureData, "pic-2")).isFalse();
    }

    @Test
    void findPictureUsages_findsPictureActionInALocationCommand() {
        LocationData location = new LocationData();
        location.setId("loc-1");

        PictureActionData pictureAction = new PictureActionData();
        pictureAction.setPictureId("pic-1");

        CommandData command = new CommandData();
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        commandDescription.setVerb(new Word("examine", Word.Type.VERB));
        command.setCommandDescription(commandDescription);
        command.getActions().add(pictureAction);

        CommandChainData chain = new CommandChainData();
        chain.getCommands().add(command);

        CommandProviderData commandProviderData = new CommandProviderData();
        commandProviderData.setAvailableCommands(new HashMap<>());
        commandProviderData.getAvailableCommands().put("examine", chain);
        location.setCommandProviderData(commandProviderData);

        adventureData.getLocationData().put(location.getId(), location);

        List<PictureUsageTracker.PictureUsage> usages =
                PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).hasSize(1);
        assertThat(usages.getFirst().getDisplayText()).contains("Picture Action");
    }

    @Test
    void countPictureUsages_returnsZero_whenNoneFound() {
        assertThat(PictureUsageTracker.countPictureUsages(adventureData, "pic-1")).isZero();
    }

    @Test
    void isPictureUsed_returnsFalse_whenNoneFound() {
        assertThat(PictureUsageTracker.isPictureUsed(adventureData, "pic-1")).isFalse();
    }

    // --- Sources beyond a location's own commands: items, exits, pocket, workflow ---

    @Test
    void findPictureUsages_findsActionInCommandOfItemLyingInALocation() {
        LocationData location = locationNamed("loc-1", "Hall");
        location.getItemContainerData().getItems().add(itemNamed("old mirror", pictureCommand("examine", "pic-1")));
        adventureData.getLocationData().put("loc-1", location);

        List<PictureUsageTracker.PictureUsage> usages = PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).singleElement().satisfies(usage ->
                assertThat(usage.getDisplayText()).isEqualTo(
                        "Picture Action: from Item 'old mirror' in 'Hall' | Command 'examine||', Action #1"));
    }

    @Test
    void findPictureUsages_findsActionInItemNestedInAContainer() {
        ItemContainerData chest = new ItemContainerData("chest");
        chest.getItems().add(itemNamed("map", pictureCommand("read", "pic-1")));
        LocationData location = locationNamed("loc-1", "Hall");
        location.getItemContainerData().getItems().add(chest);
        adventureData.getLocationData().put("loc-1", location);

        assertThat(PictureUsageTracker.countPictureUsages(adventureData, "pic-1")).isEqualTo(1);
    }

    @Test
    void findPictureUsages_findsActionInCommandOfItemCarriedInThePocket() {
        adventureData.getPlayerPocket().getItems().add(itemNamed("photo", pictureCommand("examine", "pic-1")));

        List<PictureUsageTracker.PictureUsage> usages = PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).singleElement().satisfies(usage ->
                assertThat(usage.getDisplayText()).isEqualTo(
                        "Picture Action: from Item 'photo' (in pocket) | Command 'examine||', Action #1"));
    }

    @Test
    void findPictureUsages_findsActionInExitCommand() {
        DirectionData north = new DirectionData();
        north.getDescriptionData().setShortDescription("north");
        north.setCommandData(pictureCommand("go north", "pic-1"));
        LocationData location = locationNamed("loc-1", "Hall");
        location.getDirectionsData().add(north);
        adventureData.getLocationData().put("loc-1", location);

        List<PictureUsageTracker.PictureUsage> usages = PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).singleElement().satisfies(usage ->
                assertThat(usage.getDisplayText()).isEqualTo(
                        "Picture Action: from Direction 'north' in 'Hall' | Command 'go north||', Action #1"));
    }

    @Test
    void findPictureUsages_findsActionInEachOfTheThreeWorkflowLists() {
        WorkflowData workflow = new WorkflowData();
        workflow.getCommands().add(pictureCommand("tick", "pic-1"));
        workflow.getInterceptorCommands().add(pictureCommand("shiver", "pic-1"));
        workflow.getArrivalProcesses().add(pictureCommand("welcome", "pic-1"));
        workflow.getArrivalProcesses().add(pictureCommand("other", "pic-2"));
        adventureData.setWorkflowData(workflow);

        List<PictureUsageTracker.PictureUsage> usages = PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).extracting(PictureUsageTracker.PictureUsage::getDisplayText).containsExactly(
                "Picture Action: from Workflow Process | Command 'tick||', Action #1",
                "Picture Action: from Response Process | Command 'shiver||', Action #1",
                "Picture Action: from Arrival Process | Command 'welcome||', Action #1");
    }

    @Test
    void isPictureUsed_isTrueForAPictureUsedOnlyInTheWorkflow() {
        WorkflowData workflow = new WorkflowData();
        workflow.getCommands().add(pictureCommand("tick", "workflow-only"));
        adventureData.setWorkflowData(workflow);

        // Deleting such a picture must be refused just like any other referenced one
        assertThat(PictureUsageTracker.isPictureUsed(adventureData, "workflow-only")).isTrue();
    }

    @Test
    void findPictureUsages_reportsDefaultPictureAlongsideActionUsages() {
        LocationData location = locationNamed("loc-1", "Hall");
        location.setPictureId("pic-1");
        adventureData.getLocationData().put("loc-1", location);
        adventureData.getPlayerPocket().getItems().add(itemNamed("photo", pictureCommand("examine", "pic-1")));

        List<PictureUsageTracker.PictureUsage> usages = PictureUsageTracker.findPictureUsages(adventureData, "pic-1");

        assertThat(usages).extracting(PictureUsageTracker.PictureUsage::getDisplayText).containsExactly(
                "Location Default Picture: is the default picture for 'Hall'",
                "Picture Action: from Item 'photo' (in pocket) | Command 'examine||', Action #1");
    }

    @Test
    void findPictureUsages_skipsUnresolvedItemsAndMissingPocketAndWorkflow() {
        LocationData location = locationNamed("loc-1", "Hall");
        location.getItemContainerData().getItems().add(null);
        adventureData.getLocationData().put("loc-1", location);
        adventureData.setPlayerPocket(null);
        adventureData.setWorkflowData(null);

        assertThat(PictureUsageTracker.findPictureUsages(adventureData, "pic-1")).isEmpty();
    }

    // Helper methods

    private static CommandData pictureCommand(String verb, String pictureId) {
        CommandData command = new CommandData(new CommandDescriptionData(new Word(verb, Word.Type.VERB), null, null));
        PictureActionData action = new PictureActionData();
        action.setPictureId(pictureId);
        command.addAction(action);
        return command;
    }

    private static ItemData itemNamed(String name, CommandData command) {
        ItemData item = new ItemData();
        item.getDescriptionData().setShortDescription(name);
        item.getCommandProviderData().add(command);
        return item;
    }

    private static LocationData locationNamed(String id, String description) {
        LocationData location = new LocationData();
        location.setId(id);
        location.getDescriptionData().setShortDescription(description);
        location.setItemContainerData(new ItemContainerData("floor"));
        return location;
    }
}
