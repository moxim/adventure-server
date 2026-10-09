package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.RouteIds;

class LocationMapViewTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private LocationMapView view;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new LocationMapView(accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdventureData enterAdventure(String worldMapPictureId) {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        PictureData map = new PictureData();
        map.setName("the map of the world");
        map.setContentType("image/png");
        map.setContent(new byte[] {1, 2, 3});
        adventure.getPictureData().put(map.getId(), map);
        adventure.setWorldMapPictureId(worldMapPictureId == null ? null
                                       : worldMapPictureId.equals("MAP") ? map.getId() : worldMapPictureId);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        view.beforeEnter(event);
        return adventure;
    }

    private List<Div> cells() {
        return find(Div.class, view).withClassName("world-map-cell").all();
    }

    @Test
    void showsTheChosenPictureStretchedIntoASixteenByNineFrame() {
        enterAdventure("MAP");

        Image image = find(Image.class, view).single();
        assertThat(image.getSrc()).isNotBlank();
        assertThat(image.getAlt()).hasValue("the map of the world");
        assertThat(image.getStyle().get("object-fit")).isEqualTo("fill");

        Div frame = find(Div.class, view).withClassName("world-map").single();
        assertThat(frame.getStyle().get("aspect-ratio")).isEqualTo("16 / 9");
        assertThat(frame.getStyle().get("max-width")).isEqualTo("1280px");
        assertThat(find(Button.class, view).all()).isEmpty();
    }

    @Test
    void overlaysAClickableTenByTenGrid() {
        enterAdventure("MAP");

        assertThat(cells()).hasSize(100);
    }

    @Test
    void clickingACellReportsItsColumnAndRow() {
        enterAdventure("MAP");
        Div cell = cells().get(3 * 10 + 7);

        ComponentUtil.fireEvent(cell, new ClickEvent<>(cell));

        assertThat(test(find(Notification.class).single()).getText()).isEqualTo("Location 7 : 3");
    }

    @Test
    void hintsAtTheAdventureEditorWhenNoPictureIsChosen() {
        enterAdventure(null);

        assertThat(find(Image.class, view).all()).isEmpty();
        assertThat(cells()).isEmpty();
        assertThat(view.getElement().getTextRecursively()).contains("No world map").contains("Adventure Editor");
        assertThat(find(Button.class, view).withText("Open the Adventure Editor").all()).hasSize(1);
    }

    @Test
    void hintsAtTheAdventureEditorWhenTheChosenPictureNoLongerExists() {
        enterAdventure("deleted-picture-id");

        assertThat(find(Image.class, view).all()).isEmpty();
        assertThat(find(Button.class, view).withText("Open the Adventure Editor").all()).hasSize(1);
    }

    @Test
    void reenteringWithADifferentStateReplacesWhatWasShown() {
        enterAdventure("MAP");
        enterAdventure(null);

        assertThat(find(Image.class, view).all()).isEmpty();
        assertThat(cells()).isEmpty();
    }
}
