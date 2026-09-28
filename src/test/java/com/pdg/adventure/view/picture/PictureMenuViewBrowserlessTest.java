package com.pdg.adventure.view.picture;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
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
import static org.mockito.Mockito.verify;
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
    private PictureData pictureData;
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

        pictureData = new PictureData();
        pictureData.setId("pic-1");
        pictureData.setName("Treasure chest");
        pictureData.setContentType("image/png");
        pictureData.setContent(new byte[] {1, 2, 3});

        HashMap<String, PictureData> pictures = new HashMap<>();
        pictures.put(pictureData.getId(), pictureData);
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

    // Delete goes through a GridContextMenu item click, which has no reliable way to be driven
    // from a browserless test - this instead calls the package-private confirmDeletePicture()
    // directly, exercising the real usage-guard logic in production code.
    @Test
    void deletingAPictureStillReferencedByALocation_showsTheUsageDialog_andDoesNotDelete() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        location.setPictureId("pic-1");
        adventureData.getLocationData().put(location.getId(), location);

        enter();

        view.confirmDeletePicture(pictureData);

        assertThat(adventureData.getPictureData()).containsKey("pic-1");
        assertThat(find(ConfirmDialog.class).single()).isNotNull();
    }

    // Unblocked path: no usages, so confirmDeletePicture() would build and open the dialog.
    // We drive buildDeleteConfirmDialog() directly (package-private for exactly this) since a
    // browserless test cannot reliably open dialogs triggered from a GridContextMenu item click.
    @Test
    void confirmingDeleteDialog_forAnUnusedPicture_removesIt_andPersists() {
        enter();

        ConfirmDialog dialog = view.buildDeleteConfirmDialog(pictureData);
        UI.getCurrent().add(dialog);
        dialog.open();

        test(dialog).confirm();

        assertThat(adventureData.getPictureData()).doesNotContainKey("pic-1");
        verify(adventureService).saveAdventureData(adventureData);
        verify(adventureService).deletePicture("pic-1");
    }
}
