package com.pdg.adventure.view.sound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.WorkflowData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.action.PlayActionData;

class SoundUsageTrackerTest {

    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
    }

    @Test
    void findSoundUsages_returnsEmpty_forNullAdventureNullIdOrUnusedSound() {
        assertThat(SoundUsageTracker.findSoundUsages(null, "s1")).isEmpty();
        assertThat(SoundUsageTracker.findSoundUsages(adventureData, null)).isEmpty();
        assertThat(SoundUsageTracker.countSoundUsages(adventureData, "s1")).isZero();
    }

    @Test
    void findSoundUsages_findsAPlayInALocationCommand() {
        LocationData location = locationNamed("loc-1", "Hall");
        location.getCommandProviderData().add(playCommand("listen", "s1"));
        adventureData.getLocationData().put("loc-1", location);

        List<SoundUsageTracker.SoundUsage> usages = SoundUsageTracker.findSoundUsages(adventureData, "s1");

        assertThat(usages).singleElement().satisfies(usage -> assertThat(usage.getDisplayText())
                .isEqualTo("Play Action: from 'Hall' | Command 'listen||', Action #1"));
    }

    @Test
    void findSoundUsages_findsAPlayInTheCommandOfAnItem() {
        LocationData location = locationNamed("loc-1", "Hall");
        ItemData item = new ItemData();
        item.getDescriptionData().setShortDescription("music box");
        item.getCommandProviderData().add(playCommand("wind", "s1"));
        location.getItemContainerData().getItems().add(item);
        adventureData.getLocationData().put("loc-1", location);

        assertThat(SoundUsageTracker.findSoundUsages(adventureData, "s1")).singleElement().satisfies(usage ->
                assertThat(usage.getDisplayText())
                        .isEqualTo("Play Action: from Item 'music box' in 'Hall' | Command 'wind||', Action #1"));
    }

    @Test
    void findSoundUsages_findsAPlayInAnExitCommand() {
        DirectionData north = new DirectionData();
        north.getDescriptionData().setShortDescription("north");
        north.setCommandData(playCommand("go north", "s1"));
        LocationData location = locationNamed("loc-1", "Hall");
        location.getDirectionsData().add(north);
        adventureData.getLocationData().put("loc-1", location);

        assertThat(SoundUsageTracker.countSoundUsages(adventureData, "s1")).isEqualTo(1);
    }

    @Test
    void findSoundUsages_findsAPlayInEachOfTheWorkflowLists_arrivalProcessesAndResponsesIncluded() {
        WorkflowData workflow = new WorkflowData();
        workflow.getCommands().add(playCommand("tick", "s1"));
        workflow.getInterceptorCommands().add(playCommand("shiver", "s1"));
        workflow.getArrivalProcesses().add(playCommand("welcome", "s1"));
        workflow.getArrivalProcesses().add(playCommand("other", "s2"));
        adventureData.setWorkflowData(workflow);

        assertThat(SoundUsageTracker.findSoundUsages(adventureData, "s1"))
                .extracting(SoundUsageTracker.SoundUsage::getDisplayText).containsExactly(
                        "Play Action: from Workflow Process | Command 'tick||', Action #1",
                        "Play Action: from Response Process | Command 'shiver||', Action #1",
                        "Play Action: from Arrival Process | Command 'welcome||', Action #1");
    }

    @Test
    void findSoundUsages_findsAPlayInAPocketItem() {
        ItemData item = new ItemData();
        item.getDescriptionData().setShortDescription("whistle");
        item.getCommandProviderData().add(playCommand("blow", "s1"));
        adventureData.getPlayerPocket().getItems().add(item);

        assertThat(SoundUsageTracker.findSoundUsages(adventureData, "s1")).singleElement().satisfies(usage ->
                assertThat(usage.getDisplayText())
                        .isEqualTo("Play Action: from Item 'whistle' (in pocket) | Command 'blow||', Action #1"));
    }

    @Test
    void a_PlayWithTimesZero_countsAsUseToo() {
        WorkflowData workflow = new WorkflowData();
        PlayActionData stop = new PlayActionData();
        stop.setSoundId("s1");
        stop.setTimes(0);
        CommandData command = new CommandData(new CommandDescriptionData(new Word("hush", Word.Type.VERB), null, null));
        command.addAction(stop);
        workflow.getCommands().add(command);
        adventureData.setWorkflowData(workflow);

        assertThat(SoundUsageTracker.countSoundUsages(adventureData, "s1")).isEqualTo(1);
    }

    private static CommandData playCommand(String verb, String soundId) {
        CommandData command = new CommandData(new CommandDescriptionData(new Word(verb, Word.Type.VERB), null, null));
        PlayActionData action = new PlayActionData();
        action.setSoundId(soundId);
        action.setTimes(1);
        command.addAction(action);
        return command;
    }

    private static LocationData locationNamed(String id, String description) {
        LocationData location = new LocationData();
        location.setId(id);
        location.getDescriptionData().setShortDescription(description);
        return location;
    }
}
