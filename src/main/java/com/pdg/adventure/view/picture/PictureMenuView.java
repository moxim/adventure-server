package com.pdg.adventure.view.picture;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/pictures", layout = PicturesMainLayout.class)
@PageTitle("Pictures")
@RolesAllowed("ROLE_AUTHOR")
public class PictureMenuView extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver {

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;

    private final Div gridContainer;
    private final TextField searchField;
    private final Button create;
    private final Button edit;
    private final Button backButton;
    private final Span numberOfPictures;

    private String targetPictureId;
    private transient AdventureData adventureData;
    private transient List<PictureData> pictures;

    public PictureMenuView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;

        numberOfPictures = new Span();

        edit = new Button("Edit Picture", _ -> navigateToPictureEditor(targetPictureId));
        edit.setEnabled(false);

        create = new Button("Create Picture", _ -> UI.getCurrent().navigate(PictureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));

        backButton = new Button("Back", _ -> UI.getCurrent().navigate(AdventureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(numberOfPictures, edit, create, backButton);
        leftSide.setMaxWidth("25%");
        leftSide.setMinWidth("25%");
        leftSide.setWidth("25%");

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find picture");
        searchField.setTooltipText("Find pictures by name");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(_ -> refreshGrid());

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout mainRow = new HorizontalLayout(leftSide, rightSide);
        mainRow.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(mainRow);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        adventureData = resolvedAdventure.get();
        fillGUI();
    }

    private void fillGUI() {
        pictures = new ArrayList<>(adventureData.getPictureData().values());
        numberOfPictures.setText("Pictures: " + pictures.size());
        refreshGrid();
    }

    private void refreshGrid() {
        gridContainer.removeAll();
        gridContainer.add(buildGrid());
    }

    private Grid<PictureData> buildGrid() {
        Grid<PictureData> grid = new Grid<>(PictureData.class, false);
        grid.addColumn(new ComponentRenderer<>(this::thumbnailFor)).setHeader("Preview").setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(PictureData::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(picture -> PictureUsageTracker.countPictureUsages(adventureData, picture.getId()))
            .setHeader("Used").setAutoWidth(true);
        grid.setSizeFull();
        grid.setEmptyStateText("No pictures found. Upload some to bring locations to life.");

        ListDataProvider<PictureData> dataProvider = new ListDataProvider<>(pictures);
        dataProvider.setFilter(picture -> matchesTerm(picture.getName(), searchField.getValue()));
        grid.setItems(dataProvider);

        grid.addSelectionListener(selection -> {
            Optional<PictureData> selected = selection.getFirstSelectedItem();
            if (selected.isPresent()) {
                targetPictureId = selected.get().getId();
                edit.setEnabled(true);
            } else {
                edit.setEnabled(false);
            }
        });

        grid.addItemDoubleClickListener(e -> navigateToPictureEditor(e.getItem().getId()));

        GridContextMenu<PictureData> contextMenu = new GridContextMenu<>(grid);
        contextMenu.addItem("Edit", e -> e.getItem().ifPresent(picture -> navigateToPictureEditor(picture.getId())));
        contextMenu.addItem("Find Usage", e -> e.getItem().ifPresent(this::showPictureUsage));
        contextMenu.addItem("Delete", e -> e.getItem().ifPresent(this::confirmDeletePicture));

        return grid;
    }

    private Image thumbnailFor(PictureData picture) {
        Image thumbnail = new Image();
        thumbnail.setMaxHeight("48px");
        StreamResource resource = new StreamResource(picture.getId(),
                () -> new ByteArrayInputStream(picture.getContent()));
        thumbnail.setSrc(resource);
        return thumbnail;
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return searchTerm == null || searchTerm.isBlank()
               || value.toLowerCase().contains(searchTerm.trim().toLowerCase());
    }

    private void navigateToPictureEditor(String aPictureId) {
        UI.getCurrent().navigate(PictureEditorView.class,
                                 new RouteParameters(new RouteParam(RouteIds.PICTURE_ID.getValue(), aPictureId),
                                                     new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId())));
    }

    private void showPictureUsage(PictureData aPicture) {
        List<PictureUsageTracker.PictureUsage> usages =
                PictureUsageTracker.findPictureUsages(adventureData, aPicture.getId());
        ViewSupporter.showUsages("Picture Usage", "picture", aPicture.getId(), usages);
    }

    private void confirmDeletePicture(PictureData aPicture) {
        String pictureId = aPicture.getId();
        int usageCount = PictureUsageTracker.countPictureUsages(adventureData, pictureId);

        if (usageCount > 0) {
            Notification notification = Notification.show(
                    "Cannot delete picture '" + aPicture.getName() +
                    "' because it is still referenced " + usageCount +
                    " time(s). Please remove those references first.",
                    5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            final ConfirmDialog dialog = ViewSupporter.getConfirmDialog("Delete Picture", "picture",
                                                                        aPicture.getName());
            dialog.addConfirmListener(_ -> {
                adventureData.getPictureData().remove(pictureId);
                adventureService.deletePicture(pictureId);
                adventureService.saveAdventureData(adventureData);
                fillGUI();
            });
            dialog.open();
        }
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        // no unsaved editable state lives on this view - nothing to guard.
    }
}
