package com.pdg.adventure.server.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.server.tangible.GenericContainer;

/** The picture mode through real locations: which picture is showing after each arrival and look. */
class PictureModeArrivalTest {

    private GameContext gameContext;
    private MovePlayerAction enterHall;
    private Location hall;

    @BeforeEach
    void setUp() {
        gameContext = new GameContext();
        gameContext.setUpWorkflows();
        hall = location("hall", "hall-picture");
        enterHall = new MovePlayerAction(hall, gameContext, new VariableProvider());
    }

    private static Location location(String aName, String aPictureId) {
        DescriptionProvider description = new DescriptionProvider(aName, "room");
        description.setShortDescription("The " + aName + ".");
        description.setLongDescription("The long " + aName + ".");
        Location location = new Location(description, new GenericContainer(new DescriptionProvider("items"), 5));
        location.setPictureId(aPictureId);
        return location;
    }

    private void setMode(PictureMode aMode) {
        new PictureModeAction(aMode, gameContext).execute();
    }

    @Test
    void default_showsThePictureOnTheFirstArrivalOnly_thenClearsIt() {
        enterHall.execute();
        assertThat(gameContext.getCurrentPictureId()).isEqualTo("hall-picture");

        enterHall.execute();
        assertThat(gameContext.getCurrentPictureId()).isNull();
    }

    @Test
    void on_showsThePictureOnEveryArrival() {
        setMode(PictureMode.ON);

        enterHall.execute();
        enterHall.execute();
        enterHall.execute();

        assertThat(gameContext.getCurrentPictureId()).isEqualTo("hall-picture");
    }

    @Test
    void on_stillHidesThePictureOfALocationTooDarkToSee() {
        setMode(PictureMode.ON);
        hall.setLight(0);

        enterHall.execute();

        assertThat(gameContext.getCurrentPictureId()).isNull();
    }

    @Test
    void off_showsNoPictureOnArrival_andClearsTheOneShowing() {
        gameContext.setCurrentPictureId("some-other-picture");
        setMode(PictureMode.OFF);

        enterHall.execute();

        assertThat(gameContext.getCurrentPictureId()).isNull();
    }

    @Test
    void off_aLookStillShowsThePicture() {
        setMode(PictureMode.OFF);
        enterHall.execute();

        new LookAction(gameContext).execute();

        assertThat(gameContext.getCurrentPictureId()).isEqualTo("hall-picture");
    }

    @Test
    void aPictureAction_showsItsPictureInEveryMode() {
        setMode(PictureMode.OFF);
        enterHall.execute();

        new PictureAction("poster", gameContext).execute();

        assertThat(gameContext.getCurrentPictureId()).isEqualTo("poster");
    }

    @Test
    void backToDefault_afterOn_treatsAVisitedLocationAsRepeat() {
        setMode(PictureMode.ON);
        enterHall.execute();
        setMode(PictureMode.DEFAULT);

        enterHall.execute();

        assertThat(gameContext.getCurrentPictureId()).isNull();
    }
}
