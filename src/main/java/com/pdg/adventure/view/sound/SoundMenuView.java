package com.pdg.adventure.view.sound;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.SoundService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.HistoryNavigation;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/** The list of an adventure's sounds, with a play button per sound; modelled on the picture menu. */
@Route(value = "author/adventures/:adventureId/sounds", layout = SoundsMainLayout.class)
@PageTitle("Sounds")
@RolesAllowed("ROLE_AUTHOR")
public class SoundMenuView extends VerticalLayout implements BeforeEnterObserver {

    private static final long KIBIBYTE = 1024L;
    private static final long MEBIBYTE = KIBIBYTE * KIBIBYTE;

    private final transient SoundService soundService;
    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;

    private final Div gridContainer;
    private final TextField searchField;
    private final Button create;
    private final Button edit;
    private final Button backButton;
    private final Span numberOfSounds;

    private String targetSoundId;
    private transient AdventureData adventureData;
    private transient List<SoundData> sounds;

    public SoundMenuView(SoundService aSoundService, AdventureService anAdventureService,
                         AdventureAccessService anAccessService) {
        setSizeFull();

        soundService = aSoundService;
        adventureService = anAdventureService;
        accessService = anAccessService;

        numberOfSounds = new Span();

        edit = new Button("Edit Sound", _ -> navigateToSoundEditor(targetSoundId));
        edit.setEnabled(false);

        create = new Button("Create Sound", _ -> UI.getCurrent().navigate(SoundEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));

        backButton = new Button("Back", _ -> HistoryNavigation.navigateUp(AdventureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(numberOfSounds, edit, create, backButton);
        leftSide.setMaxWidth("25%");
        leftSide.setMinWidth("25%");
        leftSide.setWidth("25%");

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find sound");
        searchField.setTooltipText("Find sounds by name");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(_ -> refreshGrid());

        gridContainer = new Div();
        gridContainer.setSizeFull();

        Span hint = new Span("Sounds must be mp3 files, at most 6MB. "
                             + "Double-click a sound to edit it, or right-click for more options.");
        hint.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        VerticalLayout rightSide = new VerticalLayout(hint, searchField, gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout mainRow = new HorizontalLayout(leftSide, rightSide);
        mainRow.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(mainRow);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure =
                AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        adventureData = resolvedAdventure.get();
        fillGUI();
    }

    private void fillGUI() {
        sounds = new ArrayList<>(adventureData.getSoundData().values());
        numberOfSounds.setText("Sounds: " + sounds.size());
        refreshGrid();
    }

    private void refreshGrid() {
        gridContainer.removeAll();
        gridContainer.add(buildGrid());
    }

    private Grid<SoundData> buildGrid() {
        Grid<SoundData> grid = new Grid<>(SoundData.class, false);
        grid.addColumn(new ComponentRenderer<>(this::playButtonFor)).setHeader("Play").setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(SoundData::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(sound -> formatSize(sound.getSizeBytes())).setHeader("Size").setAutoWidth(true);
        grid.addColumn(sound -> SoundUsageTracker.countSoundUsages(adventureData, sound.getId()))
            .setHeader("Used").setAutoWidth(true);
        grid.setSizeFull();
        grid.setEmptyStateText("No sounds found. Upload some to bring your adventure to life.");

        ListDataProvider<SoundData> dataProvider = new ListDataProvider<>(sounds);
        dataProvider.setFilter(sound -> matchesTerm(sound.getName(), searchField.getValue()));
        grid.setItems(dataProvider);

        grid.addSelectionListener(selection -> {
            Optional<SoundData> selected = selection.getFirstSelectedItem();
            if (selected.isPresent()) {
                targetSoundId = selected.get().getId();
                edit.setEnabled(true);
            } else {
                edit.setEnabled(false);
            }
        });

        grid.addItemDoubleClickListener(e -> navigateToSoundEditor(e.getItem().getId()));

        GridContextMenu<SoundData> contextMenu = new GridContextMenu<>(grid);
        contextMenu.addItem("Edit", e -> e.getItem().ifPresent(sound -> navigateToSoundEditor(sound.getId())));
        contextMenu.addItem("Find Usage", e -> e.getItem().ifPresent(this::showSoundUsage));
        contextMenu.addItem("Delete", e -> e.getItem().ifPresent(this::confirmDeleteSound));

        return grid;
    }

    /** The audio bytes are fetched only when the button is used: the content reference is lazy. */
    private SoundPlayButton playButtonFor(SoundData aSound) {
        return new SoundPlayButton(aSound::audioBytes);
    }

    /** 2048 bytes read as "2.0 KB"; sounds are at most 6 MiB. */
    static String formatSize(long aSizeBytes) {
        if (aSizeBytes >= MEBIBYTE) {
            return String.format(Locale.ROOT, "%.1f MB", aSizeBytes / (double) MEBIBYTE);
        }
        if (aSizeBytes >= KIBIBYTE) {
            return String.format(Locale.ROOT, "%.1f KB", aSizeBytes / (double) KIBIBYTE);
        }
        return aSizeBytes + " B";
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return searchTerm == null || searchTerm.isBlank()
               || value.toLowerCase().contains(searchTerm.trim().toLowerCase());
    }

    private void navigateToSoundEditor(String aSoundId) {
        UI.getCurrent().navigate(SoundEditorView.class,
                                 new RouteParameters(new RouteParam(RouteIds.SOUND_ID.getValue(), aSoundId),
                                                     new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId())));
    }

    private void showSoundUsage(SoundData aSound) {
        ViewSupporter.showUsages("Sound Usage", "sound", aSound.getName(),
                                 SoundUsageTracker.findSoundUsages(adventureData, aSound.getId()));
    }

    void confirmDeleteSound(SoundData aSound) {
        if (SoundUsageTracker.countSoundUsages(adventureData, aSound.getId()) > 0) {
            showSoundUsage(aSound);
        } else {
            buildDeleteConfirmDialog(aSound).open();
        }
    }

    // Package-private so a browserless test can drive the dialog directly - a GridContextMenu
    // item click has no reliable way to be triggered from such a test.
    ConfirmDialog buildDeleteConfirmDialog(SoundData aSound) {
        String soundId = aSound.getId();
        final ConfirmDialog dialog = ViewSupporter.getConfirmDialog("Delete Sound", "sound", aSound.getName());
        dialog.addConfirmListener(_ -> {
            adventureData.getSoundData().remove(soundId);
            adventureService.saveAdventureData(adventureData);
            soundService.delete(soundId);
            fillGUI();
        });
        return dialog;
    }
}
