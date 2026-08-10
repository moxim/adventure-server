package com.pdg.adventure.view.systemmessage;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.SystemMessageData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.support.PlaceholderSpec;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Per-adventure screen for translating/rewording the fixed catalog of built-in engine messages.
 * System messages are engine text, not author-authored content - kept in their own {@link
 * SystemMessageData} collection, separate from {@link com.pdg.adventure.model.MessageData} - but
 * still scoped to one adventure, since different adventures can run in different languages.
 * Entries can only be edited - there is deliberately no create, delete or rename anywhere in this
 * view, since the catalog itself is fixed (see {@link SystemMessageKey}).
 */
@Route(value = "author/adventures/:adventureId/system-messages", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class SystemMessagesView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final Grid<SystemMessageEntry> grid = new Grid<>(SystemMessageEntry.class, false);
    private transient AdventureData adventureData;
    private String pageTitle;

    public SystemMessagesView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        setSizeFull();

        Button backButton = new Button("Back", _ ->
                UI.getCurrent().navigate(AdventureEditorView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())))
        );
        backButton.addClickShortcut(Key.ESCAPE);

        configureGrid();

        add(new H2("System Messages"), backButton, ViewSupporter.doubleClickEditHint(), grid);
    }

    private void configureGrid() {
        grid.addColumn(SystemMessageEntry::id).setHeader("Key").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(SystemMessageEntry::defaultText).setHeader("Original (English)").setFlexGrow(2);
        grid.addColumn(SystemMessageEntry::text).setHeader("Current Text").setFlexGrow(2);
        grid.addColumn(row -> row.isEdited() ? "Yes" : "").setHeader("Edited").setAutoWidth(true).setFlexGrow(0);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        ViewSupporter.setSize(grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        setData(resolvedAdventure.get());
    }

    private void setData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        pageTitle = "System Messages for " + adventureData.getTitle();
        // Covers both a brand new adventure and one that predates a newly-added catalog key -
        // never touches an already-present (possibly admin-edited) entry.
        SystemMessageKey.seedMissingInto(adventureData.getSystemMessages(), adventureData.getId());
        updateList();
    }

    private void updateList() {
        Map<String, SystemMessageData> byKey = adventureData.getSystemMessages();
        grid.setItems(Arrays.stream(SystemMessageKey.values())
                .map(key -> byKey.get(key.id()))
                .filter(Objects::nonNull)
                .map(SystemMessageEntry::from)
                .toList());
    }

    private void openEditDialog(SystemMessageEntry aRow) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit System Message: " + aRow.id());
        dialog.setWidth("600px");

        Span originalSpan = new Span("Original (English): " + aRow.defaultText());
        Span contextSpan = new Span(aRow.description() + " — used in " + aRow.sourceLocation());
        contextSpan.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.9em");

        TextArea textArea = new TextArea("Text");
        textArea.setWidthFull();
        textArea.setMinHeight("100px");
        textArea.setValue(aRow.text());
        textArea.setRequired(true);

        Button saveBtn = new Button("Save", e -> save(aRow.id(), textArea.getValue(), dialog));
        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(new VerticalLayout(originalSpan, contextSpan, textArea, new HorizontalLayout(saveBtn, cancelBtn)));
        dialog.open();
    }

    private void save(String anId, String aNewText, Dialog aDialog) {
        try {
            validate(anId, aNewText);
            SystemMessageData message = adventureData.getSystemMessages().get(anId);
            message.setText(aNewText);
            message.touch();
            adventureService.saveAdventureData(adventureData);

            Notification notification = Notification.show("Message updated", 2000, Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateList();
            aDialog.close();
        } catch (IllegalArgumentException ex) {
            Notification notification = Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void validate(String anId, String aNewText) {
        if (aNewText == null || aNewText.isBlank()) {
            throw new IllegalArgumentException("Message text cannot be empty");
        }
        SystemMessageKey key = SystemMessageKey.fromId(anId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown system message id: " + anId));
        if (!PlaceholderSpec.isValidReplacement(key.defaultText(), aNewText)) {
            throw new IllegalArgumentException(
                    "Text must use exactly the placeholders from the original message: " + key.defaultText());
        }
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }
}
