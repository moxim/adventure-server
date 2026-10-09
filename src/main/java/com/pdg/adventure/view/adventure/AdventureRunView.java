package com.pdg.adventure.view.adventure;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;
import jakarta.annotation.security.RolesAllowed;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.server.action.MovePlayerAction;
import com.pdg.adventure.server.engine.AdventureRunSession;
import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.engine.AdventureRunSessionFactory;
import com.pdg.adventure.server.engine.FontMarkup;
import com.pdg.adventure.server.engine.RunAlreadyActiveException;
import com.pdg.adventure.server.engine.RunOwner;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.support.VariableProvider;
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
    private static final String LUMO_FONT_FAMILY = "--lumo-font-family";

    private enum Origin { EDITOR, MENU, LIBRARY }

    private final transient AdventureRunSessionFactory sessionFactory;
    private final transient AdventureAccessService accessService;
    private final transient VariableProvider variableProvider;
    private final MessageList messageList = new MessageList();
    private final MessageInput messageInput = new MessageInput();
    private final Image pictureDisplay = new Image();
    private final Div pictureContainer = new Div(pictureDisplay);

    private final RunOwner runOwner = new RunOwner(ViewSupporter.getCurrentUser().getId());

    private boolean runConflict;
    private transient AdventureRunSession session;
    private transient AdventureData adventureData;
    private String displayedPictureId;
    private String adventureId;
    private String pageTitle = "Adventure";
    private Origin origin = Origin.EDITOR;

    public AdventureRunView(AdventureRunSessionFactory aSessionFactory, AdventureAccessService anAccessService,
                            VariableProvider aVariableProvider) {
        sessionFactory = aSessionFactory;
        accessService = anAccessService;
        variableProvider = aVariableProvider;

        messageList.setSizeFull();
        messageInput.setWidthFull();
        messageInput.focus();
        messageInput.addSubmitListener(this::handleSubmit);
        // Fixed row: never let the flex algorithm shrink this to make room for playSection.
        messageInput.getStyle().set("flex-shrink", "0");

        Button backButton = new Button("Back", _ -> navigateBack());
        // Fixed row: never let the flex algorithm shrink this to make room for playSection.
        backButton.getStyle().set("flex-shrink", "0");

        // Its own scrollable region, independent of the page: as the conversation grows, this
        // scrolls internally instead of pushing the picture, Back button or MessageInput out of
        // view. Sized by playSection.expand() below - grows to fill whatever room the picture
        // (when shown) doesn't need, and shrinks to make room for it otherwise.
        VerticalLayout messageListContainer = new VerticalLayout(messageList);
        messageListContainer.setWidthFull();
        messageListContainer.setPadding(false);
        messageListContainer.getStyle().set("overflow-y", "auto");
        messageListContainer.getStyle().set("border", "1px solid #e0e0e0");

        pictureContainer.setWidthFull();
        pictureContainer.getStyle().set("flex", "0 0 auto");
        pictureContainer.setVisible(false);
        pictureDisplay.getStyle().set("max-width", "1280px");
        pictureDisplay.getStyle().set("max-height", "720px");
        pictureDisplay.getStyle().set("width", "100%");
        pictureDisplay.getStyle().set("height", "auto");
        pictureDisplay.getStyle().set("object-fit", "contain");
        pictureDisplay.getStyle().set("display", "block");
        pictureDisplay.getStyle().set("margin", "0 auto");

        // The combined picture+description section: the picture (when shown) takes only the
        // natural space it needs (capped at 1280x720 above), and the description always fills
        // whatever is left - the full section when there's no picture, a shrunk remainder when
        // there is.
        VerticalLayout playSection = new VerticalLayout(pictureContainer, messageListContainer);
        playSection.setWidthFull();
        playSection.setPadding(false);
        playSection.expand(messageListContainer);
        playSection.getStyle().set("border", "1px solid #e0e0e0");
        // A flex child's default min-height is "auto" (its content's natural size), not 0 - so
        // without this, playSection refuses to shrink below its content's height and can blow
        // out the root layout, pushing the Back button off screen (root's overflow:hidden plus
        // messageInput.focus() scrolling the page down to keep the input visible is what actually
        // hides it - see backButton/messageInput's flex-shrink:0 below for the other half of the
        // fix).
        playSection.getStyle().set("min-height", "0");

        // Back button, play section and input are direct siblings of the same fixed-height root,
        // with only the play section allowed to grow/shrink - so all three stay on screen
        // together and only messageListContainer ever scrolls internally.
        setSizeFull();
        setPadding(true);
        getStyle().set("overflow", "hidden");
        add(backButton, playSection, messageInput);
        expand(playSection);
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
        adventureData = resolvedAdventure.get();
        adventureId = adventureData.getId();
        pageTitle = (origin == Origin.LIBRARY ? "Playing: " : "Test: ") + adventureData.getTitle();
        applyFont(adventureData.getFont());

        if (session != null) {
            // The router reuses this view instance when the same route is entered again (e.g. with another
            // adventure id). Its own earlier run must not count as "another game running".
            sessionFactory.release(runOwner);
            session = null;
            messageList.setItems(List.of());
            displayedPictureId = null;
            pictureContainer.setVisible(false);
        }
        try {
            session = sessionFactory.start(adventureData, runOwner);
        } catch (RunAlreadyActiveException _) {
            // Another game of this browser session is still active - possibly in a tab that was refreshed or
            // crashed, which Vaadin only notices after missed heartbeats. On a first entry the view is not
            // attached yet, so the dialog is opened from onAttach; a reused, attached instance opens it now.
            messageInput.setEnabled(false);
            if (isAttached()) {
                openRunConflictDialog();
            } else {
                runConflict = true;
            }
            return;
        } catch (RuntimeException e) {
            FlashNotifier.flash("Could not start the adventure: " + e.getMessage());
            forwardToOrigin(event);
            return;
        }
        enableAndFocusInput();
        renderOpeningRoom();
    }

    private void renderOpeningRoom() {
        MovePlayerAction movePlayerAction = new MovePlayerAction(session.getGameContext().getCurrentLocation(),
                                                                 session.getGameContext(), variableProvider);
        ExecutionResult result = session.runBound(movePlayerAction::execute);
        renderNarratorLines(List.of(result.getResultMessage()));
        refreshPictureDisplay();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (runConflict) {
            runConflict = false;
            openRunConflictDialog();
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        runOwner.markGone();
        // On logout or session expiry Vaadin closes the session before it detaches its UIs. The session-scoped
        // ActiveRun cannot be resolved then, and it is destroyed with the session anyway.
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null && vaadinSession.getState() == VaadinSessionState.OPEN) {
            sessionFactory.release(runOwner);
        }
    }

    private void openRunConflictDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Game already running");
        dialog.setText("You already have a game running in another tab.");
        dialog.setConfirmButton("End the other game and start here", _ -> takeOverRun());
        dialog.setCancelable(true);
        dialog.setCancelButton("Back", _ -> navigateBack());
        dialog.open();
    }

    // A disabled element cannot take focus, so the constructor's focus() is lost whenever the input starts out
    // disabled (after a conflict, or after a game over on a reused view): focus it again once it is enabled.
    private void enableAndFocusInput() {
        messageInput.setEnabled(true);
        messageInput.focus();
    }

    private void takeOverRun() {
        try {
            session = sessionFactory.startReplacingActive(adventureData, runOwner);
        } catch (RuntimeException e) {
            FlashNotifier.flash("Could not start the adventure: " + e.getMessage());
            navigateBack();
            return;
        }
        enableAndFocusInput();
        renderOpeningRoom();
    }

    /**
     * Sets Lumo's font-family property on the game text and the input only (not on the Back button or
     * the page), so the rest of the app keeps its font. DEFAULT removes the override.
     * <p>
     * TODO: Review needed — scope is the message list and input; the picture and the Back button keep the app font.
     */
    private void applyFont(AdventureFont aFont) {
        for (Component gameText : List.of(messageList, messageInput)) {
            aFont.cssFontFamily().ifPresentOrElse(
                    family -> gameText.getStyle().set(LUMO_FONT_FAMILY, family),
                    () -> gameText.getStyle().remove(LUMO_FONT_FAMILY));
        }
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

        handleInput(input);
    }

    private void handleInput(final String input) {
        RunResult result = session.submit(input);
        renderNarratorLines(result.lines());
        refreshPictureDisplay();
        messageInput.setEnabled(!result.gameOver());
        if (result.gameOver()) {
            sessionFactory.release(runOwner);
        }
    }

    private void renderNarratorLines(List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        // A message with its own font wraps its text in markers (FontMarkup); each font run becomes a message
        // of its own, tagged with the font's CSS class. Text without markers is one message, as always.
        // TODO: Review needed — one message bubble per font run: a marked message between plain lines splits the
        //  turn into several bubbles instead of staying inside one.
        for (FontMarkup.Segment segment : FontMarkup.split(String.join("\n", lines))) {
            MessageListItem item = new MessageListItem(segment.text()); //, Instant.now(), NARRATOR));
            segment.font().cssClassName().ifPresent(item::addClassNames);
            messageList.addItem(item);
        }
    }

    private void refreshPictureDisplay() {
        String pictureId = session.getGameContext().getCurrentPictureId();
        if (Objects.equals(pictureId, displayedPictureId)) {
            return;
        }
        displayedPictureId = pictureId;

        PictureData picture = pictureId == null ? null : adventureData.getPictureData().get(pictureId);
        if (picture == null) {
            pictureContainer.setVisible(false);
            return;
        }

        pictureDisplay.setSrc(event -> {
            event.inline();
            event.setContentType(picture.getContentType());
            event.getOutputStream().write(picture.getContent());
        });
        pictureContainer.setVisible(true);
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
