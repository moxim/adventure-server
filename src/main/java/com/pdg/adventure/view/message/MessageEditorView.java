package com.pdg.adventure.view.message;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.component.AdventureFontSelect;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "author/adventures/:adventureId/messages/:messageId/edit", layout = MessagesMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/messages/new", layout = MessagesMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class MessageEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(MessageEditorView.class);
    private static final String FONT_WEIGHT_TEXT = "font-weight";
    private static final String FONT_STYLE_TEXT = "font-style";
    private static final String FONT_COLOR_TEXT = "color";
    private static final String FONT_SECONDARY_COLOR_TEXT = "var(--lumo-secondary-text-color)";

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final Binder<MessageViewModel> binder;

    private Button saveButton;
    private Button resetButton;
    private final TextArea messageTextField;
    private final Select<AdventureFont> fontSelect;
    private final Div previewDiv;
    private final Div usageInfoDiv;
    private String pageTitle;

    private transient String messageId;
    private transient AdventureData adventureData;
    private transient MessageViewModel mvm;

    public MessageEditorView(AdventureService anAdventureService, AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        accessService = anAccessService;
        binder = new Binder<>(MessageViewModel.class);

        // Build UI
        setSizeFull();
        H4 title = new H4("Message Editor");

        final TextField summaryField;
        summaryField = new TextField("Summary");
        summaryField.setPlaceholder("e.g., Welcome to the island, The door is locked");
        summaryField.setHelperText("A short note on what the message says - helps you find it again");
        summaryField.setWidthFull();
        summaryField.setRequired(true);
        summaryField.setValueChangeMode(ValueChangeMode.EAGER);

        messageTextField = new TextArea("Message Text");
        messageTextField.setPlaceholder("Enter the message text that will be displayed to the player");
        messageTextField.setWidthFull();
        messageTextField.setRequired(true);
        messageTextField.setMinHeight("150px");
        messageTextField.setMaxHeight("400px");
        messageTextField.setValueChangeMode(ValueChangeMode.EAGER);

        fontSelect = new AdventureFontSelect("Message Font", "Same as adventure");
        fontSelect.setHelperText("The font this message is shown in when the adventure runs");
        fontSelect.setWidth("280px");

        // Preview section
        Span previewLabel = new Span("Preview:");
        previewLabel.getStyle().set(FONT_WEIGHT_TEXT, "bold");
        previewDiv = new Div();
        previewDiv.setId("message-preview");
        previewDiv.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                  .set("border-radius", "var(--lumo-border-radius-m)").set("padding", "var(--lumo-space-m)")
                  .set("background-color", "var(--lumo-contrast-5pct)").set("min-height", "60px")
                  .set("margin-top", "var(--lumo-space-s)");

        VerticalLayout previewSection = new VerticalLayout(previewLabel, previewDiv);
        previewSection.setPadding(false);
        previewSection.setSpacing(false);

        // Usage info section
        Span usageLabel = new Span("Usage:");
        usageLabel.getStyle().set(FONT_WEIGHT_TEXT, "bold");
        usageInfoDiv = new Div();
        usageInfoDiv.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("border-radius", "var(--lumo-border-radius-m)").set("padding", "var(--lumo-space-m)")
                    .set("background-color", "var(--lumo-contrast-5pct)").set("margin-top", "var(--lumo-space-s)");

        VerticalLayout usageSection = new VerticalLayout(usageLabel, usageInfoDiv);
        usageSection.setPadding(false);
        usageSection.setSpacing(false);

        final ResetBackSaveView resetBackSaveView = setUpNavigationButtons();

        // Bind fields
        binder.forField(summaryField).asRequired("Summary is required")
              .withValidator(summary -> summary != null && !summary.trim().isEmpty(), "Summary cannot be empty")
              .bind(MessageViewModel::getSummary, MessageViewModel::setSummary);

        binder.forField(messageTextField).asRequired("Message text is required")
              .withValidator(text -> text != null && !text.trim().isEmpty(), "Message text cannot be empty")
              .bind(MessageViewModel::getMessageText, MessageViewModel::setMessageText);

        binder.bind(fontSelect, MessageViewModel::getFont, MessageViewModel::setFont);

        // Update preview when message text or font changes
        messageTextField.addValueChangeListener(_ -> updatePreview());
        fontSelect.addValueChangeListener(_ -> updatePreview());

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
            resetButton.setEnabled(hasChanges);
        });

        add(title, summaryField, messageTextField, fontSelect, previewSection, usageSection, resetBackSaveView);
    }

    private ResetBackSaveView setUpNavigationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        saveButton = resetBackSaveView.getSave();
        saveButton.setEnabled(false);
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);

        backButton.addClickListener(_ -> navigateBack());
        saveButton.addClickListener(_ -> validateAndSave());
        resetButton.addClickListener(_ -> {
            binder.readBean(mvm);
            updatePreview();
        });
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void navigateBack() {
        UI.getCurrent().navigate(MessagesMenuView.class, new RouteParameters(
                  new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
    }

    private void updatePreview() {
        // "Same as adventure" previews in the adventure's own font - what a player will actually see
        // (the select has no value yet while the binder is still filling in the other fields)
        AdventureFont chosen = Objects.requireNonNullElse(fontSelect.getValue(), AdventureFont.DEFAULT);
        AdventureFont shown = chosen != AdventureFont.DEFAULT || adventureData == null
                              ? chosen : adventureData.getFont();
        shown.cssFontFamily().ifPresentOrElse(family -> previewDiv.getStyle().set("font-family", family),
                                               () -> previewDiv.getStyle().remove("font-family"));
        String text = messageTextField.getValue();
        if (text == null || text.trim().isEmpty()) {
            previewDiv.setText("(empty message)");
            previewDiv.getStyle().set(FONT_STYLE_TEXT, "italic").set(FONT_COLOR_TEXT, FONT_SECONDARY_COLOR_TEXT);
        } else {
            previewDiv.setText(text);
            previewDiv.getStyle().set(FONT_STYLE_TEXT, "normal").set(FONT_COLOR_TEXT, "var(--lumo-body-text-color)");
        }
    }

    private void validateAndSave() {
        try {
            if (binder.validate().isOk()) {
                binder.writeBean(mvm);

                // Create or update message
                MessageData message = createRequiredMessage();

                // Add/update message in adventure's messages Map, keyed by its id
                adventureData.getMessages().put(message.getId(), message);

                // Save adventure (triggers cascade save for message via @CascadeSave)
                adventureService.saveAdventureData(adventureData);

                // Update tracking variables
                mvm.setId(message.getId());
                messageId = message.getId();

                // Mark as no longer new
                mvm.setNew(false);

                // Update usage info
                updateUsageInfo();

                navigateBack();
            }
        } catch (ValidationException | IllegalArgumentException e) {
            LOG.error(e.getMessage());
            Notification notification = Notification.show("Error saving message: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private MessageData createRequiredMessage() {
        if (mvm.isNew()) {
            MessageData created = new MessageData(mvm.getSummary(), mvm.getMessageText());
            created.setFont(mvm.getFont());
            return created;
        }
        MessageData message = adventureData.getMessages().get(mvm.getId());
        message.setSummary(mvm.getSummary());
        message.setText(mvm.getMessageText());
        message.setFont(mvm.getFont());
        message.touch();
        return message;
    }

    private void updateUsageInfo() {
        if (messageId == null || messageId.isEmpty()) {
            usageInfoDiv.removeAll();
            usageInfoDiv.add(new Span("This is a new message. Usage information will be available after saving."));
            usageInfoDiv.getStyle().set(FONT_STYLE_TEXT, "italic").set(FONT_COLOR_TEXT, FONT_SECONDARY_COLOR_TEXT);
            return;
        }

        List<MessageUsageTracker.MessageUsage> usages = MessageUsageTracker.findMessageUsages(adventureData, messageId);

        usageInfoDiv.removeAll();
        usageInfoDiv.getStyle().set(FONT_STYLE_TEXT, "normal").set(FONT_COLOR_TEXT, "var(--lumo-body-text-color)");

        if (usages.isEmpty()) {
            Span noUsageSpan = new Span("This message is not currently used anywhere in the adventure.");
            noUsageSpan.getStyle().set(FONT_COLOR_TEXT, FONT_SECONDARY_COLOR_TEXT);
            usageInfoDiv.add(noUsageSpan);
        } else {
            Span usageCountSpan = new Span("Used in " + usages.size() + " location(s):");
            usageCountSpan.getStyle().set(FONT_WEIGHT_TEXT, "bold").set("display", "block")
                          .set("margin-bottom", "0.5em");
            usageInfoDiv.add(usageCountSpan);

            VerticalLayout usageList = new VerticalLayout();
            usageList.setPadding(false);
            usageList.setSpacing(false);
            usageList.getStyle().set("margin-left", "1em");

            for (MessageUsageTracker.MessageUsage usage : usages) {
                Span usageItem = new Span("• " + usage.getDisplayText());
                usageItem.getStyle().set("font-size", "0.9em").set("display", "block");
                usageList.add(usageItem);
            }

            usageInfoDiv.add(usageList);
        }
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        // The message id is a ULID, so the route parameter needs no percent-decoding.
        messageId = event.getRouteParameters().get(RouteIds.MESSAGE_ID.getValue()).orElse(null);
        setData(resolvedAdventure.get());
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    private void setData(AdventureData anAdventureData) {
        adventureData = anAdventureData;

        // Load existing message or create new one
        MessageData existing = messageId == null ? null : adventureData.getMessages().get(messageId);
        // TODO: Review needed — a route id that matches no message (e.g. a stale link to a deleted
        //  message) silently opens an empty "new message" form; should it notify and forward instead?
        if (existing != null) {
            mvm = new MessageViewModel(existing);
            pageTitle = "Edit Message: " + mvm.getSummary();
        } else {
            messageId = null;
            mvm = new MessageViewModel();
            pageTitle = "New Message";
        }

        binder.readBean(mvm);
        updatePreview();
        updateUsageInfo();
        saveButton.setEnabled(false);
        resetButton.setEnabled(false);
    }
}
