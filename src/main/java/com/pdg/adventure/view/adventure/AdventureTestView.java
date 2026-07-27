package com.pdg.adventure.view.adventure;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.engine.AdventureTestSession;
import com.pdg.adventure.server.engine.AdventureTestSession.TestResult;
import com.pdg.adventure.server.engine.AdventureTestSessionFactory;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/test", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class AdventureTestView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    private static final String NARRATOR = "Narrator";

    private final transient AdventureTestSessionFactory sessionFactory;
    private final transient AdventureAccessService accessService;
    private final MessageList messageList = new MessageList();
    private final MessageInput messageInput = new MessageInput();

    private transient AdventureTestSession session;
    private String adventureId;
    private String pageTitle = "Test Adventure";

    public AdventureTestView(AdventureTestSessionFactory aSessionFactory, AdventureAccessService anAccessService) {
        sessionFactory = aSessionFactory;
        accessService = anAccessService;

        messageList.setSizeFull();
        messageInput.setWidthFull();
        messageInput.addSubmitListener(this::handleSubmit);

        Button backButton = new Button("Back", _ -> navigateBack());

        VerticalLayout chatLayout = new VerticalLayout(messageList, messageInput);
        chatLayout.setSizeFull();
        chatLayout.setPadding(false);
        chatLayout.expand(messageList);

        setSizeFull();
        setPadding(true);
        add(backButton, chatLayout);
        expand(chatLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure =
                AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        AdventureData adventureData = resolvedAdventure.get();
        adventureId = adventureData.getId();
        pageTitle = "Test: " + adventureData.getTitle();

        try {
            session = sessionFactory.start(adventureData);
        } catch (RuntimeException e) {
            FlashNotifier.flash("Could not start test session: " + e.getMessage());
            event.forwardTo(AdventureEditorView.class, routeParameters(adventureId));
            return;
        }

        TestResult opening = session.submit("look");
        renderNarratorLines(opening.lines());
        messageInput.setEnabled(!opening.gameOver());
    }

    private void handleSubmit(MessageInput.SubmitEvent event) {
        if (session == null) {
            return;
        }
        String input = event.getValue();
        messageList.addItem(new MessageListItem(input, Instant.now(), ViewSupporter.getCurrentUser().getUsername()));

        TestResult result = session.submit(input);
        renderNarratorLines(result.lines());
        messageInput.setEnabled(!result.gameOver());
    }

    private void renderNarratorLines(List<String> lines) {
        for (String line : lines) {
            messageList.addItem(new MessageListItem(line, Instant.now(), NARRATOR));
        }
    }

    private void navigateBack() {
        getUI().ifPresent(ui -> ui.navigate(AdventureEditorView.class, routeParameters(adventureId)));
    }

    private static RouteParameters routeParameters(String anAdventureId) {
        return new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), anAdventureId));
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }
}
