package com.pdg.adventure.view.adventure;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.exception.AdventureImportException;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@PageTitle("Your World Of Adventures")
@Route(value = "author/adventures", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class AdventuresMenuView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(AdventuresMenuView.class);

    private static final String EXPORT_FILE_SUFFIX = ".adventure.json";

    private final transient AdventureAccessService accessService;

    private String targetAdventureId;
    private final Button runAdventure;
    private final Anchor exportLink = new Anchor();
    private Grid<AdventureData> adventureGrid;

    public AdventuresMenuView(AdventureAccessService anAccessService) {

        setSizeFull();

        accessService = anAccessService;

        Button create = new Button("Create Adventure", _ -> UI.getCurrent().navigate(AdventureEditorView.class));
        create.addClassName("adventures-menu-view-button-1");

        runAdventure = new Button("Run Adventure");
        runAdventure.setEnabled(false);
        runAdventure.addClickListener(_ -> navigateToAdventureRun(targetAdventureId));

        Button importAdventure = new Button("Import Adventure", _ -> buildImportDialog().open());

        VerticalLayout leftSide = new VerticalLayout(create, importAdventure, runAdventure);

        // One hidden link serves every export: exportAdventure points it at the file and clicks it
        exportLink.getStyle().set("display", "none");
        exportLink.setRouterIgnore(true);
        leftSide.add(exportLink);

        List<AdventureData> adventures = accessService.getAdventuresForUser(ViewSupporter.getCurrentUser());
        Div gridContainer = getGridContainer(adventures);
        VerticalLayout rightSide = new VerticalLayout(ViewSupporter.doubleClickEditHint(), gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);

        add(jumpRow);
    }

    private Div getGridContainer(List<AdventureData> adventures) {
        Grid<AdventureData> grid = new Grid<>(AdventureData.class, false);
        adventureGrid = grid;
        grid.addColumn(AdventureData::getTitle).setHeader("Title").setSortable(true).setAutoWidth(true);
        grid.addSelectionListener(selection -> {
            Optional<AdventureData> optionalAdventure = selection.getFirstSelectedItem();
            if (optionalAdventure.isPresent()) {
                targetAdventureId = optionalAdventure.get().getId();
                ViewSupporter.enableIfStartable(runAdventure, optionalAdventure.get(), null);
            } else {
                runAdventure.setEnabled(false);
            }
        });
        grid.addItemDoubleClickListener(e -> {
            targetAdventureId = e.getItem().getId();
            navigateToAdventureEditor(targetAdventureId);
        });

        grid.setItems(adventures);

        ViewSupporter.setSize(grid);
        grid.setEmptyStateText("Create some adventures.");

        new AdventureDataContextMenu(grid);

        Div gridContainer = new Div(grid);
        gridContainer.setSizeFull();

        return gridContainer;
    }

    private void navigateToAdventureEditor(String aTargetAdventureId) {
        UI.getCurrent().navigate(AdventureEditorView.class,
                                 new RouteParameters(RouteIds.ADVENTURE_ID.getValue(), aTargetAdventureId));
    }

    private void navigateToAdventureRun(String aTargetAdventureId) {
        UI.getCurrent().navigate(AdventureRunView.menuRunPath(aTargetAdventureId));
    }

    private class AdventureDataContextMenu extends GridContextMenu<AdventureData> {
        public AdventureDataContextMenu(Grid<AdventureData> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(adventure -> {
                targetAdventureId = adventure.getId();
                navigateToAdventureEditor(targetAdventureId);
            }));

            addItem("Duplicate", e -> e.getItem().ifPresent(adventure -> duplicateAdventure(adventure, target)));

            addItem("Export", e -> e.getItem().ifPresent(AdventuresMenuView.this::exportAdventure));

            addComponent(new Hr());

            GridMenuItem<AdventureData> adventureDetailItem =
                    addItem("AdventureId", e -> e.getItem().ifPresent(adventure -> adventure.getId()));

            setDynamicContentHandler(adventure -> {
                if (adventure == null) return false;
                adventureDetailItem.scrollIntoView();
                adventureDetailItem.setText(adventure.getNotes());
                return true;
            });

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(adventure ->
                    buildDeleteConfirmDialog(adventure, target).open()));
        }
    }

    /**
     * Package-private for testing, like {@link #buildDeleteConfirmDialog}: the context-menu click
     * that triggers it can't be driven from a browserless test.
     */
    @SuppressWarnings("unchecked")
    void duplicateAdventure(AdventureData adventure, Grid<AdventureData> grid) {
        try {
            AdventureData copy = accessService.duplicateAdventure(adventure.getId(), ViewSupporter.getCurrentUser());

            ListDataProvider<AdventureData> dataProvider = (ListDataProvider<AdventureData>) grid.getDataProvider();
            dataProvider.getItems().add(copy);
            dataProvider.refreshAll();

            Notification notification = Notification.show("Adventure '" + adventure.getTitle()
                    + "' duplicated as '" + copy.getTitle() + "'.", 2000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (RuntimeException e) {
            LOG.error("Could not duplicate adventure {}", adventure.getId(), e);
            Notification notification = Notification.show("Could not duplicate adventure '"
                    + adventure.getTitle() + "'.", 5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Package-private for testing, like {@link #duplicateAdventure}. Fetches the file on the UI thread, so a refusal is
     * shown as a notification, then points the hidden link at it and clicks it for the browser download.
     */
    void exportAdventure(AdventureData anAdventure) {
        try {
            byte[] json = accessService.exportAdventure(anAdventure.getId(), ViewSupporter.getCurrentUser());
            String fileName = exportFileName(anAdventure.getTitle());
            exportLink.setHref(DownloadHandler.fromInputStream(
                    _ -> new DownloadResponse(new ByteArrayInputStream(json), fileName, "application/json",
                                              json.length)));
            exportLink.getElement().executeJs("this.click()");
        } catch (RuntimeException e) {
            LOG.error("Could not export adventure {}", anAdventure.getId(), e);
            showError("Could not export adventure '" + anAdventure.getTitle() + "'.");
        }
    }

    /** A title made safe as a file name: letters and digits kept, every other run of characters becomes one dash. */
    static String exportFileName(String aTitle) {
        String name = aTitle == null ? "" : aTitle.trim().replaceAll("[^\\p{L}\\p{N}]+", "-").replaceAll("(^-)|(-$)", "");
        return (name.isEmpty() ? "adventure" : name) + EXPORT_FILE_SUFFIX;
    }

    /**
     * Package-private for testing. The upload only collects the bytes (its callback runs without the UI lock); the
     * Import button reads them on the UI thread once the upload has finished.
     */
    Dialog buildImportDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Import Adventure");

        AtomicReference<byte[]> uploaded = new AtomicReference<>();
        long maxBytes = accessService.getMaxImportBytes();
        Upload upload = new Upload(importUploadHandler(uploaded, maxBytes));
        upload.setMaxFiles(1);
        upload.setAcceptedFileExtensions(".json");
        if (maxBytes > 0) {
            upload.setMaxFileSize((int) Math.min(Integer.MAX_VALUE, maxBytes));
        }

        Button importButton = new Button("Import", _ -> {
            byte[] data = uploaded.get();
            if (data != null && importAdventure(data)) {
                dialog.close();
            }
        });
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.setEnabled(false);
        upload.addAllFinishedListener(_ -> importButton.setEnabled(uploaded.get() != null));
        upload.addFileRemovedListener(_ -> {
            uploaded.set(null);
            importButton.setEnabled(false);
        });

        dialog.add(upload);
        dialog.getFooter().add(new Button("Cancel", _ -> dialog.close()), importButton);
        return dialog;
    }

    /**
     * Package-private for testing. {@link Upload#setMaxFileSize} is only checked by the browser, so the handler
     * itself refuses a body larger than the import limit: otherwise any author could make the server buffer an
     * arbitrarily large request in memory.
     */
    static UploadHandler importUploadHandler(AtomicReference<byte[]> aTarget, long aMaxBytes) {
        return new InMemoryUploadHandler((_, data) -> aTarget.set(data)) {
            @Override
            public long getFileSizeMax() {
                return aMaxBytes;
            }

            @Override
            public long getRequestSizeMax() {
                return aMaxBytes;
            }

            @Override
            public long getFileCountMax() {
                return 1;
            }
        };
    }

    /**
     * Package-private for testing.
     *
     * @return true if the adventure was imported
     */
    @SuppressWarnings("unchecked")
    boolean importAdventure(byte[] aFile) {
        try {
            AdventureAccessService.ImportedAdventure imported =
                    accessService.importAdventure(aFile, ViewSupporter.getCurrentUser());

            ListDataProvider<AdventureData> dataProvider = (ListDataProvider<AdventureData>) adventureGrid.getDataProvider();
            dataProvider.getItems().add(imported.adventure());
            dataProvider.refreshAll();

            String text = "Adventure '" + imported.adventure().getTitle() + "' imported.";
            if (imported.builderVersionDiffers()) {
                text += " The file was written by another version of the builder - please check the adventure.";
            }
            Notification notification = Notification.show(text, imported.builderVersionDiffers() ? 6000 : 2000,
                                                          Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;
        } catch (AdventureImportException e) {
            showError("Could not import the adventure: " + e.getMessage());
            return false;
        } catch (RuntimeException e) {
            LOG.error("Could not import an adventure", e);
            showError("Could not import the adventure.");
            return false;
        }
    }

    private static void showError(String aText) {
        Notification notification = Notification.show(aText, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Package-private for testing: GridContextMenu item clicks have no reliable way to be
     * driven from a browserless test, so this builds the dialog (and wires its confirm
     * listener) in isolation from the context-menu click that triggers it.
     */
    @SuppressWarnings("unchecked")
    ConfirmDialog buildDeleteConfirmDialog(AdventureData adventure, Grid<AdventureData> grid) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Adventure");
        dialog.setText("Are you sure you want to delete '" + adventure.getTitle()
                + "'? This cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(_ -> {
            ListDataProvider<AdventureData> dataProvider =
                    (ListDataProvider<AdventureData>) grid.getDataProvider();
            dataProvider.getItems().remove(adventure);
            dataProvider.refreshAll();
            accessService.deleteAdventure(adventure.getId(), ViewSupporter.getCurrentUser());

            Notification notification = Notification.show("Adventure '" + adventure.getTitle()
                    + "' deleted successfully.", 2000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        return dialog;
    }
}
