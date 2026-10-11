package com.pdg.adventure.view.picture;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.streams.UploadEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Test
    void uploadingOnAnExistingPicture_thenClickingReset_doesNotMutateTheLivePictureData() {
        enterWithPictureId("pic-1");

        view.stagePendingUpload(new byte[] {9, 9, 9}, "image/webp");

        // Also change the name so the Reset button becomes enabled (its enablement is driven by
        // the binder's status-change listener, which only fires on a bound-field change).
        TextField nameField = find(TextField.class, view).all().getFirst();
        test(nameField).setValue("Updated name");

        Button reset = find(Button.class, view).withText("Reset").single();
        test(reset).click();

        assertThat(pictureData.getContent()).isEqualTo(new byte[] {1, 2, 3});
        assertThat(pictureData.getContentType()).isEqualTo("image/png");
    }

    // Driving Vaadin's Upload end-to-end isn't practical in a browserless test, so these hand the
    // view's upload handler a mocked UploadEvent, the way the framework would after a request.
    private UploadEvent uploadEventWith(InputStream aStream) {
        UploadEvent event = mock(UploadEvent.class);
        when(event.getInputStream()).thenReturn(aStream);
        when(event.getUI()).thenReturn(UI.getCurrent());
        return event;
    }

    private static byte[] pngBytes(int aTotalLength) {
        byte[] bytes = new byte[aTotalLength];
        System.arraycopy(new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}, 0, bytes, 0, 8);
        return bytes;
    }

    @Test
    void handleUpload_withAValidImage_stagesItAndShowsThePreview() {
        enterWithPictureId(null);
        UploadEvent event = uploadEventWith(new ByteArrayInputStream(pngBytes(64)));

        view.handleUpload(event);

        assertThat(find(Notification.class).all()).isEmpty();
        assertThat(find(Image.class, view).all()).hasSize(1);
    }

    @Test
    void handleUpload_withAFileThatIsNotAnImage_showsAnErrorNotification() {
        enterWithPictureId(null);
        UploadEvent event = uploadEventWith(new ByteArrayInputStream("not an image".getBytes()));

        view.handleUpload(event);

        assertThat(test(find(Notification.class).single()).getText()).isEqualTo("The uploaded file is not a recognized PNG, JPEG, or WebP image.");
        assertThat(find(Image.class, view).all()).isEmpty();
    }

    @Test
    void handleUpload_withAFileOverTwoMegabytes_showsAnErrorNotification() {
        enterWithPictureId(null);
        UploadEvent event = uploadEventWith(new ByteArrayInputStream(pngBytes(2 * 1024 * 1024 + 1)));

        view.handleUpload(event);

        assertThat(test(find(Notification.class).single()).getText()).isEqualTo("Pictures must be less than 2MB in size");
        assertThat(find(Image.class, view).all()).isEmpty();
    }

    @Test
    void handleUpload_withAFileOfExactlyTwoMegabytes_isAccepted() {
        enterWithPictureId(null);
        UploadEvent event = uploadEventWith(new ByteArrayInputStream(pngBytes(2 * 1024 * 1024)));

        view.handleUpload(event);

        assertThat(find(Notification.class).all()).isEmpty();
        assertThat(find(Image.class, view).all()).hasSize(1);
    }

    @Test
    void handleUpload_whenTheStreamCannotBeRead_showsAnErrorNotification() {
        enterWithPictureId(null);
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("disk on fire");
            }
        };
        UploadEvent event = uploadEventWith(broken);

        view.handleUpload(event);

        assertThat(test(find(Notification.class).single()).getText()).isEqualTo("Could not read the uploaded file.");
        assertThat(find(Image.class, view).all()).isEmpty();
    }
}
