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
}
