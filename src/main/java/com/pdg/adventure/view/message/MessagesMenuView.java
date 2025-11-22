package com.pdg.adventure.view.message;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.MessageService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "adventures/:adventureId/messages", layout = MessagesMainLayout.class)
public class MessagesMenuView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {
    private final transient MessageService messageService;
    private final transient AdventureService adventureService;
    private final Grid<MessageViewModel> grid;
    private transient AdventureData adventureData;
    private String pageTitle;
    private transient ListDataProvider<MessageViewModel> dataProvider;

    @Autowired
    public MessagesMenuView(MessageService aMessageService, AdventureService anAdventureService) {
        messageService = aMessageService;
        adventureService = anAdventureService;
        setSizeFull();

        Button backButton = new Button("Back", event ->
                UI.getCurrent().navigate(AdventureEditorView.class,
                                         new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                            adventureData.getId())))
        );
        backButton.addClickShortcut(Key.ESCAPE);

        Button createButton = new Button("Create Message", e ->
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

        VerticalLayout rightSide = new VerticalLayout(searchField, grid);
        rightSide.setSizeFull();

        HorizontalLayout mainLayout = new HorizontalLayout(leftSide, rightSide);
        mainLayout.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(mainLayout);

        // Update message count when data is set
        grid.getDataProvider().addDataProviderListener(event -> {
            int count = grid.getDataProvider().size(new com.vaadin.flow.data.provider.Query<>());
            messageCount.setText("Total messages: " + count);
        });
    }

    private Grid<MessageViewModel> createGrid() {
        Grid<MessageViewModel> messageGrid = new Grid<>(MessageViewModel.class, false);

        messageGrid.addColumn(MessageViewModel::getId)
                   .setHeader("Message ID")
                   .setAutoWidth(true)
                   .setFlexGrow(1)
                   .setSortable(true);

        messageGrid.addColumn(mvm -> mvm.getPreview(30))
                   .setHeader("Message Text")
                   .setAutoWidth(true)
                   .setFlexGrow(3)
                   .setSortable(true);

        messageGrid.addColumn(mvm -> mvm.getMessageText().length())
                   .setHeader("Length")
                   .setAutoWidth(true)
                   .setFlexGrow(0);

        messageGrid.addColumn(MessageViewModel::getUsageCount)
                   .setHeader("Used")
                   .setAutoWidth(true)
                   .setFlexGrow(0)
                   .setSortable(true);

        // Double-click to edit
        messageGrid.addItemDoubleClickListener(e ->
                                                       UI.getCurrent().navigate(MessageEditorView.class,
                                                                                new RouteParameters(
                                                                                        new RouteParam(
                                                                                                RouteIds.ADVENTURE_ID.getValue(),
                                                                                                adventureData.getId()),
                                                                                        new RouteParam(
                                                                                                RouteIds.MESSAGE_ID.getValue(),
                                                                                                e.getItem().getId())
                                                                                ))
                                                         .ifPresent(editor -> editor.setData(adventureData))
        );

        // Context menu
        createContextMenu(messageGrid);

        ViewSupporter.setSize(messageGrid);
        messageGrid.setEmptyStateText("No messages yet. Create one to get started.");

        return messageGrid;
    }

    private void createContextMenu(Grid<MessageViewModel> messageGrid) {
        GridContextMenu<MessageViewModel> contextMenu = messageGrid.addContextMenu();

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

        // Update the message text when the context menu opens
        contextMenu.addGridContextMenuOpenedListener(event -> {
            event.getItem().ifPresent(message -> {
                messageTextSpan.setText("\"" + message.getMessageText() + "\"");
            });
        });

        // Add the message text at the top
        contextMenu.addComponentAsFirst(messageTextSpan);

        // Add separator
        contextMenu.addComponentAsFirst(new Hr());

        contextMenu.addItem("Edit", event -> {
            event.getItem().ifPresent(item ->
                                              UI.getCurrent().navigate(MessageEditorView.class,
                                                                       new RouteParameters(
                                                                               new RouteParam(
                                                                                       RouteIds.ADVENTURE_ID.getValue(),
                                                                                       adventureData.getId()),
                                                                               new RouteParam(
                                                                                       RouteIds.MESSAGE_ID.getValue(),
                                                                                       item.getId())
                                                                       ))
                                                .ifPresent(editor -> editor.setData(adventureData))
            );
        });

        contextMenu.addItem("Find Usage", event -> {
            event.getItem().ifPresent(this::showMessageUsage);
        });

        contextMenu.addItem("Duplicate", event -> {
            event.getItem().ifPresent(this::duplicateMessage);
        });

        contextMenu.addItem("Delete", event -> {
            event.getItem().ifPresent(this::confirmDeleteMessage);
        });
    }

    private void duplicateMessage(MessageViewModel original) {
        String newId = original.getId() + "_copy";
        int counter = 1;

        // Find a unique ID
        while (adventureData.getMessages().containsKey(newId)) {
            newId = original.getId() + "_copy" + counter++;
        }

        // Create the duplicate
        MessageData newMessage = new MessageData(
                adventureData.getId(),
                newId,
                original.getMessageText()
        );

        // Copy additional fields if present
        if (original.getCategory() != null) {
            newMessage.setCategory(original.getCategory());
        }
        if (original.getNotes() != null) {
            newMessage.setNotes(original.getNotes());
        }

        // Add message to adventure's messages Map
        adventureData.getMessages().put(newId, newMessage);

        // Save adventure (triggers cascade save for message via @CascadeSave)
        adventureService.saveAdventureData(adventureData);

        // Refresh the grid
        refreshGrid();

        // Navigate to edit the new message
        UI.getCurrent().navigate(MessageEditorView.class,
                                 new RouteParameters(
                                         new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                                         new RouteParam(RouteIds.MESSAGE_ID.getValue(), newId)
                                 ))
          .ifPresent(editor -> editor.setData(adventureData));
    }

    private void showMessageUsage(MessageViewModel message) {
        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData,
                                                                                              message.getId());
        ViewSupporter.showUsages("Message Usage", "message", message.getId(), usages);
    }

    private void confirmDeleteMessage(MessageViewModel message) {
        String messageId = message.getId();
        int usageCount = MessageUsageTracker.countMessageUsages(adventureData, messageId);

        if (usageCount > 0) {
            Notification.show("Cannot delete message '" + messageId +
                              "' because it is stille referenced " + usageCount +
                              " times(s). . Please remove those references first.",
                              5000, Notification.Position.MIDDLE);
        } else {
            final var dialog = getConfirmDialog(message);
            dialog.addConfirmListener(event -> {
                // Remove message from adventure's messages Map
                adventureData.getMessages().remove(messageId);

                // Delete the message document from the database
                messageService.deleteMessage(adventureData.getId(), messageId);

                // Save adventure to update @DBRef references
                adventureService.saveAdventureData(adventureData);

                refreshGrid();
            });

            dialog.open();
        }
    }

    private static ConfirmDialog getConfirmDialog(final MessageViewModel aMessage) {
        return ViewSupporter.getConfirmDialog("Delete Message", "message", aMessage.getId());
    }

    private void filterMessages(String searchTerm) {
        if (dataProvider != null) {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                dataProvider.clearFilters();
            } else {
                String lowerCaseSearchTerm = searchTerm.toLowerCase();
                dataProvider.setFilter(mvm ->
                                               mvm.getId().toLowerCase().contains(lowerCaseSearchTerm) ||
                                               mvm.getMessageText().toLowerCase().contains(lowerCaseSearchTerm)
                );
            }
        }
    }

    private void refreshGrid() {
        if (adventureData != null) {
            // Load messages from adventure's messages Map (loaded via @DBRef)
            List<MessageData> messageDataList = new ArrayList<>(adventureData.getMessages().values());

            // Convert to view models with usage counts
            List<MessageViewModel> messages = messageDataList.stream()
                                                             .map(msgData -> {
                                                                 int usageCount
                                                                         = MessageUsageTracker.countMessageUsages(
                                                                         adventureData, msgData.getMessageId());
                                                                 return new MessageViewModel(msgData, usageCount);
                                                             })
                                                             .toList();

            dataProvider = new ListDataProvider<>(messages);
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
