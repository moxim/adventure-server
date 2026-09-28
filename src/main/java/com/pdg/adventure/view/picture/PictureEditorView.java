package com.pdg.adventure.view.picture;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "author/adventures/:adventureId/pictures/:pictureId/edit", layout = PicturesMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/pictures/new", layout = PicturesMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class PictureEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(PictureEditorView.class);
    private static final int MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024;

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final Binder<PictureViewModel> binder;
    private final Image preview = new Image();
    private final Upload upload;
    private final MemoryBuffer uploadBuffer = new MemoryBuffer();

    private Button saveButton;
    private Button resetButton;
    private String pageTitle;
    private boolean hasPendingUpload;
    private byte[] pendingContent;
    private String pendingContentType;

    private transient String pictureId;
    private transient PictureData pictureData;
    private transient PictureViewModel pvm;
    private transient AdventureData adventureData;

    public PictureEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(PictureViewModel.class);

        pictureData = new PictureData();
        pictureId = pictureData.getId();

        TextField nameField = new TextField("Name");
        nameField.setWidthFull();

        upload = new Upload(uploadBuffer);
        upload.setAcceptedFileTypes("image/png", "image/webp", "image/jpeg");
        upload.setMaxFileSize(MAX_FILE_SIZE_BYTES);
        upload.setMaxFiles(1);

        preview.setMaxWidth("300px");
        preview.setVisible(false);

        upload.addSucceededListener(event -> {
            try {
                byte[] bytes = uploadBuffer.getInputStream().readAllBytes();
                if (bytes.length > MAX_FILE_SIZE_BYTES) {
                    Notification notification = Notification.show("The uploaded file is too large.", 5000,
                                                                   Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                String sniffedContentType = sniffContentType(bytes);
                if (sniffedContentType == null) {
                    Notification notification = Notification.show(
                            "The uploaded file is not a recognized PNG, JPEG, or WebP image.", 5000,
                            Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                stagePendingUpload(bytes, sniffedContentType);
                saveButton.setEnabled(binder.isValid());
            } catch (IOException e) {
                LOG.error("Failed to read uploaded picture", e);
                Notification notification = Notification.show("Could not read the uploaded file.", 5000,
                                                               Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRejectedListener(event -> {
            Notification notification = Notification.show(event.getErrorMessage(), 5000,
                                                           Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        final ResetBackSaveView resetBackSaveView = setUpNavigationButtons();

        binder.forField(nameField).asRequired("Name is required")
              .bind(PictureViewModel::getName, PictureViewModel::setName);

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid()
                               && (pictureData.getContent() != null || pendingContent != null);
            boolean hasChanges = event.getBinder().hasChanges() || hasPendingUpload;
            saveButton.setEnabled(hasChanges && isValid);
            resetButton.setEnabled(hasChanges);
        });

        setMargin(true);
        setPadding(true);

        HorizontalLayout uploadRow = new HorizontalLayout(upload, preview);
        add(nameField, uploadRow, resetBackSaveView);
    }

    private static String sniffContentType(byte[] bytes) {
        if (bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }
        if (bytes.length >= 3 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
            && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    // Package-private so a browserless test can exercise the real staging path directly, since
    // driving Vaadin's Upload component end-to-end is impractical in that environment.
    void stagePendingUpload(byte[] bytes, String contentType) {
        pendingContent = bytes;
        pendingContentType = contentType;
        hasPendingUpload = true;
        updatePreview();
    }

    private void updatePreview() {
        byte[] content = pendingContent != null ? pendingContent : pictureData.getContent();
        if (content == null) {
            preview.setVisible(false);
            return;
        }
        StreamResource resource = new StreamResource(pictureData.getId(), () -> new ByteArrayInputStream(content));
        preview.setSrc(resource);
        preview.setVisible(true);
    }

    private ResetBackSaveView setUpNavigationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        saveButton = resetBackSaveView.getSave();
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);
        saveButton.setEnabled(false);

        backButton.addClickListener(_ -> navigateBack());
        saveButton.addClickListener(_ -> validateSave(pvm));
        resetButton.addClickListener(_ -> {
            binder.readBean(pvm);
            hasPendingUpload = false;
            pendingContent = null;
            pendingContentType = null;
            updatePreview();
        });
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void navigateBack() {
        UI.getCurrent().navigate(PictureMenuView.class,
                                 new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId())));
    }

    private void validateSave(PictureViewModel aPictureViewModel) {
        try {
            if (binder.validate().isOk() && (pictureData.getContent() != null || pendingContent != null)) {
                if (hasPendingUpload) {
                    pictureData.setContent(pendingContent);
                    pictureData.setContentType(pendingContentType);
                }
                binder.writeBean(aPictureViewModel);
                final PictureData data = aPictureViewModel.getData();
                adventureData.getPictureData().put(aPictureViewModel.getId(), data);
                adventureService.savePictureData(data);
                adventureService.saveAdventureData(adventureData);
                hasPendingUpload = false;
                saveButton.setEnabled(false);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            Notification notification = Notification.show("Could not save the picture: " + e.getMessage(), 5000,
                                                           Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        final Optional<String> optionalPictureId = event.getRouteParameters().get(RouteIds.PICTURE_ID.getValue());
        optionalPictureId.ifPresent(id -> pictureId = id);
        setData(resolvedAdventure.get());
        pageTitle = optionalPictureId.isPresent() ? "Edit Picture: " + pictureData.getName() : "New Picture";
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges() || hasPendingUpload);
    }

    private void setData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        pictureData = adventureData.getPictureData().getOrDefault(pictureId, new PictureData());
        pictureId = pictureData.getId();

        saveButton.setEnabled(false);
        hasPendingUpload = false;
        pendingContent = null;
        pendingContentType = null;
        pvm = new PictureViewModel(pictureData);
        pvm.setAdventureId(adventureData.getId());

        binder.readBean(pvm);
        updatePreview();
    }
}
