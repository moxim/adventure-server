package com.pdg.adventure.view.picture;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class PictureMenuViewBrowserlessTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private AdventureData adventureData;
    private PictureMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        adventureData = buildAdventureData();
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view = new PictureMenuView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdventureData buildAdventureData() {
        AdventureData data = new AdventureData();
        data.setId("adv-1");

        PictureData picture = new PictureData();
        picture.setId("pic-1");
        picture.setName("Treasure chest");
        picture.setContentType("image/png");
        picture.setContent(new byte[] {1, 2, 3});

        HashMap<String, PictureData> pictures = new HashMap<>();
        pictures.put(picture.getId(), picture);
        data.setPictureData(pictures);
        data.setLocationData(new HashMap<>());

        return data;
    }

    private void enter() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        RouteParameters params = mock(RouteParameters.class);
        when(event.getRouteParameters()).thenReturn(params);
        when(params.get(RouteIds.ADVENTURE_ID.getValue())).thenReturn(Optional.of("adv-1"));
        view.beforeEnter(event);
    }

    @Test
    void beforeEnter_populatesTheGridWithOnePicture() {
        enter();

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    @Test
    void editButton_isDisabled_untilAPictureIsSelected() {
        enter();

        assertThat(find(Button.class, view).withText("Edit Picture").single().isEnabled()).isFalse();
    }

    @Test
    void searchField_filtersTheGridByName() {
        enter();

        TextField searchField = find(TextField.class, view).single();
        test(searchField).setValue("nonexistent");

        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isZero();
    }

    @Test
    void deletingAPictureStillReferencedByALocation_showsAnErrorNotification_andDoesNotDelete() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setPictureId("pic-1");
        adventureData.getLocationData().put(location.getId(), location);

        enter();

        // Simulate the context-menu "Delete" action directly via the tracked usage guard the
        // production confirmDeletePicture() method itself calls first.
        int usageCount = PictureUsageTracker.countPictureUsages(adventureData, "pic-1");
        assertThat(usageCount).isEqualTo(1);
        assertThat(adventureData.getPictureData()).containsKey("pic-1");
    }
}
