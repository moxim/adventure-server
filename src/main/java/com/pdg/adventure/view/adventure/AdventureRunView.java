package com.pdg.adventure.view.adventure;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.RolesAllowed;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.engine.AdventureRunSession;
import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.engine.AdventureRunSessionFactory;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.player.PlayerLibraryView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;


/**
 * Interactive play, reached three ways: an author "testing" their own adventure from
 * AdventureEditorView, an author/admin running one from AdventuresMenuView, or a player running
 * one they're assigned to from PlayerLibraryView. Same session, same UI in all three cases —
 * only the route and the Back destination differ.
 * <p>
 * Callers MUST navigate here via the static *Path(...) methods below (a path string, not
 * {@code navigate(AdventureRunView.class, ...)}). Two route templates are registered for this
 * class with an identically-shaped :adventureId parameter, so Vaadin's outbound URL generation
 * for class-based navigation can't tell them apart and always resolves to the primary @Route —
 * silently sending players to an author-only URL. Navigating by literal path string sidesteps
 * that: incoming route matching (unlike outbound URL generation) does correctly consider
 * aliases. The two author-side origins share one path, so they're disambiguated with a
 * "from=menu" query parameter instead.
 */
@Route(value = "author/adventures/:adventureId/test", layout = AdventuresMainLayout.class)
@RouteAlias(value = "player/library/:adventureId/run", layout = AdventuresMainLayout.class)
@RolesAllowed({"ROLE_AUTHOR", "ROLE_PLAYER"})
public class AdventureRunView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    private static final String NARRATOR = "Narrator";
    private static final String PLAYER_ROUTE_PREFIX = "player/";
    private static final String FROM_QUERY_PARAM = "from";
    private static final String FROM_MENU = "menu";

    private enum Origin { EDITOR, MENU, LIBRARY }

    private final transient AdventureRunSessionFactory sessionFactory;
    private final transient AdventureAccessService accessService;
    private final MessageList messageList = new MessageList();
    private final MessageInput messageInput = new MessageInput();

    private transient AdventureRunSession session;
    private String adventureId;
    private String pageTitle = "Adventure";
    private Origin origin = Origin.EDITOR;

    public AdventureRunView(AdventureRunSessionFactory aSessionFactory, AdventureAccessService anAccessService) {
        sessionFactory = aSessionFactory;
        accessService = anAccessService;

        messageList.setSizeFull();
        messageInput.setWidthFull();
        messageInput.focus();
        messageInput.addSubmitListener(this::handleSubmit);

        Button backButton = new Button("Back", _ -> navigateBack());

        // Its own scrollable region, independent of the page: as the conversation grows, this
        // scrolls internally instead of pushing the Back button or MessageInput out of view.
        VerticalLayout messageListContainer = new VerticalLayout(messageList);
        messageListContainer.setSizeFull();
        messageListContainer.setMaxHeight("80%");
        messageListContainer.setPadding(false);
        messageListContainer.getStyle().set("overflow-y", "auto");
        messageListContainer.getStyle().set("border", "1px solid #e0e0e0");

        VerticalLayout chatLayout = new VerticalLayout(messageListContainer, messageInput);
        chatLayout.setSizeFull();
        chatLayout.setPadding(false);
        chatLayout.expand(messageListContainer);

        setSizeFull();
        setPadding(true);
        add(backButton, chatLayout);
        expand(chatLayout);
    }

    /** AdventureEditorView's "Test" button should navigate here. */
    public static String editorTestPath(String anAdventureId) {
        return "author/adventures/%s/test".formatted(anAdventureId);
    }

    /** AdventuresMenuView's "Run Adventure" button should navigate here. */
    public static String menuRunPath(String anAdventureId) {
        return "author/adventures/%s/test?%s=%s".formatted(anAdventureId, FROM_QUERY_PARAM, FROM_MENU);
    }

    /** PlayerLibraryView's "Run Adventure" button (and double-click) should navigate here. */
    public static String libraryRunPath(String anAdventureId) {
        return "player/library/%s/run".formatted(anAdventureId);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        origin = resolveOrigin(event.getLocation());

        // Not using AdventureRouteResolver.resolveAdventureOrForward here: it always forwards to
        // the author-only AdventuresMenuView, which a pure PLAYER can't access. Resolve without
        // forwarding, then send the user back to wherever they actually came from.
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            // No adventureId to route back to an editor-with-id URL, so EDITOR and MENU both
            // land on the author's adventure list here.
            if (origin == Origin.LIBRARY) {
                event.forwardTo(PlayerLibraryView.class);
            } else {
                event.forwardTo(AdventuresMenuView.class);
            }
            return;
        }
        AdventureData adventureData = resolvedAdventure.get();
        adventureId = adventureData.getId();
        pageTitle = (origin == Origin.LIBRARY ? "Playing: " : "Test: ") + adventureData.getTitle();

        try {
            session = sessionFactory.start(adventureData);
        } catch (RuntimeException e) {
            FlashNotifier.flash("Could not start the adventure: " + e.getMessage());
            forwardToOrigin(event);
            return;
        }

        RunResult opening = session.submit("look");
        renderNarratorLines(opening.lines());
        messageInput.setEnabled(!opening.gameOver());
    }

    private static Origin resolveOrigin(Location location) {
        if (location.getPath().startsWith(PLAYER_ROUTE_PREFIX)) {
            return Origin.LIBRARY;
        }
        List<String> from = location.getQueryParameters().getParameters().getOrDefault(FROM_QUERY_PARAM, List.of());
        return from.contains(FROM_MENU) ? Origin.MENU : Origin.EDITOR;
    }

    private void handleSubmit(MessageInput.SubmitEvent event) {
        if (session == null) {
            return;
        }
        String input = event.getValue();
        messageList.addItem(new MessageListItem(input, Instant.now(), ViewSupporter.getCurrentUser().getUsername()));

        RunResult result = session.submit(input);
        renderNarratorLines(result.lines());
        messageInput.setEnabled(!result.gameOver());
    }

    private void renderNarratorLines(List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        messageList.addItem(new MessageListItem(String.join("\n", lines), Instant.now(), NARRATOR));
    }

    private void navigateBack() {
        getUI().ifPresent(ui -> {
            switch (origin) {
                case LIBRARY -> ui.navigate(PlayerLibraryView.class);
                case MENU -> ui.navigate(AdventuresMenuView.class);
                case EDITOR -> ui.navigate(AdventureEditorView.class, routeParameters(adventureId));
            }
        });
    }

    private void forwardToOrigin(BeforeEnterEvent event) {
        switch (origin) {
            case LIBRARY -> event.forwardTo(PlayerLibraryView.class);
            case MENU -> event.forwardTo(AdventuresMenuView.class);
            case EDITOR -> event.forwardTo(AdventureEditorView.class, routeParameters(adventureId));
        }
    }

    private static RouteParameters routeParameters(String anAdventureId) {
        return new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), anAdventureId));
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }
}
