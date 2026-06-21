package com.pdg.adventure.view.message;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.MessageService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.GridProvider;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/messages", layout = MessagesMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class MessagesMenuView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {
    private final transient MessageService messageService;
    private final transient AdventureService adventureService;
    private final Grid<MessageDescriptionAdapter> grid;
    private transient AdventureData adventureData;
    private String pageTitle;
    private transient ListDataProvider<MessageDescriptionAdapter> dataProvider;

    public MessagesMenuView(MessageService aMessageService, AdventureService anAdventureService) {
        messageService = aMessageService;
        adventureService = anAdventureService;
        setSizeFull();

        Button backButton = new Button("Back", _ ->
                UI.getCurrent().navigate(AdventureEditorView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())))
        );
        backButton.addClickShortcut(Key.ESCAPE);

        Button createButton = new Button("Create Message", _ ->
                UI.getCurrent().navigate(MessageEditorView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())))
                  .ifPresent(editor -> editor.setData(adventureData))
        );
        createButton.setIcon(new Icon(VaadinIcon.PLUS));

        // Message count indicator
        Span messageCount = new Span();
        messageCount.getStyle().set("align-self", "center");

        VerticalLayout leftSide = new VerticalLayout(createButton, backButton, messageCount);
        leftSide.setMaxWidth("30%");

        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search messages");
        searchField.setTooltipText("Find messages by ID or text content");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> filterMessages(e.getValue()));

        grid = createGrid();

        VerticalLayout rightSide = new VerticalLayout(searchField, ViewSupporter.doubleClickEditHint(), grid);
        rightSide.setSizeFull();

        HorizontalLayout mainLayout = new HorizontalLayout(leftSide, rightSide);
        mainLayout.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(mainLayout);

        // Update message count when data is set
        grid.getDataProvider().addDataProviderListener(_ -> {
            int count = grid.getDataProvider().size(new com.vaadin.flow.data.provider.Query<>());
            messageCount.setText("Total messages: " + count);
        });
    }

    private Grid<MessageDescriptionAdapter> createGrid() {
        GridProvider<MessageDescriptionAdapter> gridProvider = new GridProvider<>(MessageDescriptionAdapter.class);
        gridProvider.getGrid().getColumns().get(0).setHeader("Message ID").setFlexGrow(1);
        gridProvider.getGrid().getColumns().get(1).setHeader("Message Text").setFlexGrow(3).setSortable(true);
        gridProvider.addColumn(MessageDescriptionAdapter::getLength, "Length");

        Span usedHeader = new Span("Used");
        usedHeader.getElement().setAttribute("title", "How many commands reference this message");
        gridProvider.getGrid().addColumn(MessageDescriptionAdapter::getUsageCount).setHeader(usedHeader).setAutoWidth(true);

        gridProvider.addItemDoubleClickListener(e ->
                UI.getCurrent().navigate(MessageEditorView.class,
                                         new RouteParameters(
                                                 new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                adventureData.getId()),
                                                 new RouteParam(RouteIds.MESSAGE_ID.getValue(),
                                                                e.getItem().getId())))
                  .ifPresent(editor -> editor.setData(adventureData))
        );

        Grid<MessageDescriptionAdapter> messageGrid = gridProvider.getGrid();
        createContextMenu(messageGrid);
        ViewSupporter.setSize(messageGrid);
        messageGrid.setEmptyStateText("No messages yet. Create one to get started.");
        return messageGrid;
    }

    private void createContextMenu(Grid<MessageDescriptionAdapter> messageGrid) {
        GridContextMenu<MessageDescriptionAdapter> contextMenu = messageGrid.addContextMenu();

        // Create a span to display the full message text
        Span messageTextSpan = new Span();
        messageTextSpan.getStyle()
                       .set("font-style", "italic")
                       .set("color", "var(--lumo-secondary-text-color)")
                       .set("padding", "var(--lumo-space-s)")
                       .set("display", "block")
                       .set("max-width", "400px")
                       .set("white-space", "normal")
                       .set("word-wrap", "break-word");

        contextMenu.addGridContextMenuOpenedListener(event -> {
            event.getItem().ifPresent(adapter -> {
                messageTextSpan.setText("\"" + adapter.getMessageViewModel().getMessageText() + "\"");
            });
        });

        contextMenu.addComponentAsFirst(messageTextSpan);
        contextMenu.addComponentAsFirst(new Hr());

        contextMenu.addItem("Edit", event -> {
            event.getItem().ifPresent(adapter ->
                    UI.getCurrent().navigate(MessageEditorView.class,
                                             new RouteParameters(
                                                     new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId()),
                                                     new RouteParam(RouteIds.MESSAGE_ID.getValue(),
                                                                    adapter.getId())))
                      .ifPresent(editor -> editor.setData(adventureData))
            );
        });

        contextMenu.addItem("Find Usage", event -> event.getItem().ifPresent(this::showMessageUsage));
        contextMenu.addItem("Duplicate", event -> event.getItem().ifPresent(this::duplicateMessage));
        contextMenu.addItem("Delete", event -> event.getItem().ifPresent(this::confirmDeleteMessage));
    }

    private void duplicateMessage(MessageDescriptionAdapter adapter) {
        MessageViewModel original = adapter.getMessageViewModel();
        String newId = original.getId() + "_copy";
        int counter = 1;

        while (adventureData.getMessages().containsKey(newId)) {
            newId = original.getId() + "_copy" + counter++;
        }

        MessageData newMessage = new MessageData(adventureData.getId(), newId, original.getMessageText());

        if (original.getCategory() != null) {
            newMessage.setCategory(original.getCategory());
        }
        if (original.getNotes() != null) {
            newMessage.setNotes(original.getNotes());
        }

        adventureData.getMessages().put(newId, newMessage);
        adventureService.saveAdventureData(adventureData);
        refreshGrid();

        UI.getCurrent().navigate(MessageEditorView.class,
                                 new RouteParameters(
                                         new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                                         new RouteParam(RouteIds.MESSAGE_ID.getValue(), newId)))
          .ifPresent(editor -> editor.setData(adventureData));
    }

    private void showMessageUsage(MessageDescriptionAdapter adapter) {
        String messageId = adapter.getId();
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData,
                                                                                              messageId);
        ViewSupporter.showUsages("Message Usage", "message", messageId, usages);
    }

    private void confirmDeleteMessage(MessageDescriptionAdapter adapter) {
        String messageId = adapter.getId();
        int usageCount = MessageUsageTracker.countMessageUsages(adventureData, messageId);

        if (usageCount > 0) {
            Notification.show("Cannot delete message '" + messageId +
                              "' because it is stille referenced " + usageCount +
                              " times(s). . Please remove those references first.",
                              5000, Notification.Position.MIDDLE);
        } else {
            final var dialog = ViewSupporter.getConfirmDialog("Delete Message", "message", messageId);
            dialog.addConfirmListener(_ -> {
                adventureData.getMessages().remove(messageId);
                messageService.deleteMessage(adventureData.getId(), messageId);
                adventureService.saveAdventureData(adventureData);
                refreshGrid();
            });
            dialog.open();
        }
    }

    private void filterMessages(String searchTerm) {
        if (dataProvider != null) {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                dataProvider.clearFilters();
            } else {
                String lowerCaseSearchTerm = searchTerm.toLowerCase();
                dataProvider.setFilter(adapter ->
                        adapter.getId().toLowerCase().contains(lowerCaseSearchTerm) ||
                        adapter.getMessageViewModel().getMessageText().toLowerCase().contains(lowerCaseSearchTerm)
                );
            }
        }
    }

    private void refreshGrid() {
        if (adventureData != null) {
            List<MessageData> messageDataList = new ArrayList<>(adventureData.getMessages().values());

            List<MessageDescriptionAdapter> adapters = messageDataList.stream()
                    .map(msgData -> {
                        int usageCount = MessageUsageTracker.countMessageUsages(adventureData,
                                                                                msgData.getMessageId());
                        return new MessageDescriptionAdapter(new MessageViewModel(msgData, usageCount));
                    })
                    .toList();

            dataProvider = new ListDataProvider<>(adapters);
            grid.setDataProvider(dataProvider);
        }
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> adventureId = event.getRouteParameters().get(RouteIds.ADVENTURE_ID.getValue());
        if (adventureId.isPresent()) {
            pageTitle = "Messages for Adventure #" + adventureId.get();
        } else {
            pageTitle = "Messages";
        }
    }

    public void setData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        refreshGrid();
    }
}
