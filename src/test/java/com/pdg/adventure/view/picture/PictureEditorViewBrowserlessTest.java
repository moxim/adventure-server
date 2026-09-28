package com.pdg.adventure.view.picture;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class PictureEditorViewBrowserlessTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private AdventureData adventureData;
    private PictureData pictureData;
    private PictureEditorView view;

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
        view = new PictureEditorView(adventureService, accessService);
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

        return data;
    }

    private void enterWithPictureId(String pictureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        RouteParameters params = mock(RouteParameters.class);
        when(event.getRouteParameters()).thenReturn(params);
        when(params.get(RouteIds.ADVENTURE_ID.getValue())).thenReturn(Optional.of("adv-1"));
        when(params.get(RouteIds.PICTURE_ID.getValue())).thenReturn(Optional.ofNullable(pictureId));
        view.beforeEnter(event);
    }

    @Test
    void saveButton_isDisabled_afterSetData() {
        enterWithPictureId(null);
        assertThat(find(Button.class, view).withText("Save").single().isEnabled()).isFalse();
    }

    @Test
    void resetButton_isDisabled_afterSetData() {
        enterWithPictureId(null);
        assertThat(find(Button.class, view).withText("Reset").single().isEnabled()).isFalse();
    }

    @Test
    void beforeEnter_withPictureId_setsEditPageTitle() {
        enterWithPictureId("pic-1");
        assertThat(view.getPageTitle()).contains("Edit Picture");
    }

    @Test
    void beforeEnter_withoutPictureId_setsNewPageTitle() {
        enterWithPictureId(null);
        assertThat(view.getPageTitle()).isEqualTo("New Picture");
    }

    @Test
    void nameChange_aloneDoesNotEnableSave_withoutAPictureUploaded() {
        enterWithPictureId(null);

        TextField nameField = find(TextField.class, view).all().getFirst();
        test(nameField).setValue("A new name");

        assertThat(find(Button.class, view).withText("Save").single().isEnabled()).isFalse();
    }

    @Test
    void nameChange_onAnExistingPicture_enablesResetButton() {
        enterWithPictureId("pic-1");

        Button reset = find(Button.class, view).withText("Reset").single();
        assertThat(reset.isEnabled()).isFalse();

        TextField nameField = find(TextField.class, view).all().getFirst();
        test(nameField).setValue("Updated name");

        assertThat(reset.isEnabled()).isTrue();
    }
}
