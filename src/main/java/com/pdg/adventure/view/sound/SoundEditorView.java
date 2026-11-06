package com.pdg.adventure.view.sound;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.streams.UploadEvent;

import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.SoundService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.HistoryNavigation;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/** Uploads, renames, replaces and deletes one mp3 sound of an adventure; modelled on the picture editor. */
@Route(value = "author/adventures/:adventureId/sounds/:soundId/edit", layout = SoundsMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/sounds/new", layout = SoundsMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class SoundEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(SoundEditorView.class);
    private static final String NOT_AN_MP3 = "The uploaded file is not a valid mp3 file.";

    private final transient SoundService soundService;
    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final Binder<SoundViewModel> binder;
    private final Upload upload;
    private final SoundPlayButton playButton;
    private final Button deleteButton = new Button("Delete");
    private Button saveButton;
    private Button resetButton;
    private String pageTitle;
    private boolean hasPendingUpload;
    private byte[] pendingContent;
    private transient String soundId;
    private transient SoundData soundData;
    private transient SoundViewModel svm;
    private transient AdventureData adventureData;

    public SoundEditorView(SoundService aSoundService, AdventureService anAdventureService,
                           AdventureAccessService anAccessService) {
        setSizeFull();

        soundService = aSoundService;
        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(SoundViewModel.class);

        soundData = new SoundData();
        soundId = soundData.getId();

        TextField nameField = new TextField("Name");
        nameField.setWidthFull();

        upload = new Upload(this::handleUpload);
        upload.setAcceptedFileTypes("audio/mpeg", ".mp3");
        upload.setMaxFileSize(Mp3Format.MAX_BYTES);
        upload.setMaxFiles(1);
        upload.addFileRejectedListener(event -> showError(event.getErrorMessage()));

        playButton = new SoundPlayButton(this::currentBytes);
        playButton.setVisible(false);

        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(_ -> onDelete());

        final ResetBackSaveView resetBackSaveView = setUpNavigationButtons();

        binder.forField(nameField)
              .asRequired("Name is required")
              .withValidator(name -> !name.isBlank(), "Name is required")
              .withValidator(name -> !SoundService.isNameTaken(adventureData == null ? null : adventureData.getSoundData(),
                                                               name, soundId),
                             "A sound with this name already exists")
              .bind(SoundViewModel::getName, SoundViewModel::setName);

        binder.addStatusChangeListener(_ -> updateButtons());

        setMargin(true);
        setPadding(true);

        add(nameField, upload, new HorizontalLayout(new Span("Listen:"), playButton), deleteButton,
            resetBackSaveView);
    }

    /** The bytes the play button plays: the staged upload, else the stored sound. */
    private byte[] currentBytes() {
        if (pendingContent != null) {
            return pendingContent;
        }
        return soundData.audioBytes();
    }

    private boolean hasContent() {
        return pendingContent != null || soundData.getContent() != null;
    }

    private void updateButtons() {
        boolean valid = binder.isValid() && hasContent();
        boolean hasChanges = binder.hasChanges() || hasPendingUpload;
        saveButton.setEnabled(hasChanges && valid);
        resetButton.setEnabled(hasChanges);
    }

    /**
     * Receives one uploaded file. Runs on the request thread without the UI lock, so the checks only look at the
     * bytes and the UI is touched solely through {@code access}. Package-private so a browserless test can drive it
     * with a mocked {@link UploadEvent}.
     */
    void handleUpload(UploadEvent event) {
        byte[] bytes;
        try (InputStream in = event.getInputStream()) {
            // One byte more than allowed, so an oversized file is noticed without buffering all of it.
            bytes = in.readNBytes(Mp3Format.MAX_BYTES + 1);
        } catch (IOException e) {
            LOG.error("Failed to read uploaded sound", e);
            showUploadError(event.getUI(), "Could not read the uploaded file.");
            return;
        }
        if (bytes.length > Mp3Format.MAX_BYTES) {
            showUploadError(event.getUI(), "Sounds must be mp3 files of at most 6MB");
            return;
        }
        if (!Mp3Format.looksLikeMp3(bytes)) {
            showUploadError(event.getUI(), NOT_AN_MP3);
            return;
        }
        event.getUI().access(() -> stagePendingUpload(bytes));
    }

    private static void showUploadError(UI aUi, String aMessage) {
        aUi.access(() -> showError(aMessage));
    }

    private static void showError(String aMessage) {
        Notification notification = Notification.show(aMessage, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // Package-private so a browserless test can exercise the staging path directly.
    void stagePendingUpload(byte[] bytes) {
        pendingContent = bytes;
        hasPendingUpload = true;
        playButton.refresh();
        playButton.setVisible(true);
        updateButtons();
    }

    private ResetBackSaveView setUpNavigationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        saveButton = resetBackSaveView.getSave();
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);
        saveButton.setEnabled(false);

        backButton.addClickListener(_ -> navigateBack());
        saveButton.addClickListener(_ -> validateSave(svm));
        resetButton.addClickListener(_ -> {
            binder.readBean(svm);
            clearPendingUpload();
            playButton.refresh();
            playButton.setVisible(soundData.getContent() != null);
            updateButtons();
        });
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void clearPendingUpload() {
        hasPendingUpload = false;
        pendingContent = null;
    }

    private void navigateBack() {
        HistoryNavigation.navigateUp(SoundMenuView.class, adventureRoute(), binder.hasChanges() || hasPendingUpload);
    }

    private RouteParameters adventureRoute() {
        return new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()));
    }

    private void validateSave(SoundViewModel aSoundViewModel) {
        try {
            if (binder.validate().isOk() && hasContent()) {
                binder.writeBean(aSoundViewModel);
                final SoundData data = aSoundViewModel.getData();
                soundService.save(data, hasPendingUpload ? pendingContent : null);
                adventureData.getSoundData().put(aSoundViewModel.getId(), data);
                adventureService.saveAdventureData(adventureData);
                clearPendingUpload();
                saveButton.setEnabled(false);
                deleteButton.setEnabled(true);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            showError("Could not save the sound: " + e.getMessage());
        }
    }

    private void onDelete() {
        int usages = SoundUsageTracker.countSoundUsages(adventureData, soundId);
        if (usages > 0) {
            ViewSupporter.showUsages("Sound Usage", "sound", soundData.getName(),
                                     SoundUsageTracker.findSoundUsages(adventureData, soundId));
        } else {
            buildDeleteConfirmDialog().open();
        }
    }

    // Package-private so a browserless test can confirm the dialog directly.
    ConfirmDialog buildDeleteConfirmDialog() {
        final String idToDelete = soundId;
        final ConfirmDialog dialog = ViewSupporter.getConfirmDialog("Delete Sound", "sound", soundData.getName());
        dialog.addConfirmListener(_ -> {
            adventureData.getSoundData().remove(idToDelete);
            adventureService.saveAdventureData(adventureData);
            soundService.delete(idToDelete);
            clearPendingUpload();
            HistoryNavigation.navigateUp(SoundMenuView.class, adventureRoute());
        });
        return dialog;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure =
                AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        final Optional<String> optionalSoundId = event.getRouteParameters().get(RouteIds.SOUND_ID.getValue());
        optionalSoundId.ifPresent(id -> soundId = id);
        setData(resolvedAdventure.get());
        pageTitle = optionalSoundId.isPresent() ? "Edit Sound: " + soundData.getName() : "New Sound";
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
        soundData = adventureData.getSoundData().getOrDefault(soundId, new SoundData());
        soundId = soundData.getId();

        clearPendingUpload();
        svm = new SoundViewModel(soundData);
        svm.setAdventureId(adventureData.getId());

        binder.readBean(svm);
        boolean stored = adventureData.getSoundData().containsKey(soundId);
        deleteButton.setEnabled(stored);
        playButton.refresh();
        playButton.setVisible(soundData.getContent() != null);
        saveButton.setEnabled(false);
        resetButton.setEnabled(false);
    }
}
