package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.engine.AdventureRunSession;
import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.engine.AdventureRunSessionFactory;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.engine.RunAlreadyActiveException;
import com.pdg.adventure.server.engine.RunOwner;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.message.SystemMessageKey;
import com.pdg.adventure.server.support.VariableProvider;
import com.pdg.adventure.view.player.PlayerLibraryView;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class AdventureRunViewTest extends BrowserlessTest {

    private AdventureRunSessionFactory sessionFactory;
    private AdventureAccessService accessService;
    private AdventureRunSession session;
    private GameContext gameContext;
    private AdventureData adventureData;
    private AdventureRunView view;

    @BeforeEach
    void setUp() {
        sessionFactory = mock(AdventureRunSessionFactory.class);
        accessService = mock(AdventureAccessService.class);
        session = mock(AdventureRunSession.class);
        gameContext = mock(GameContext.class);

        adventureData = new AdventureData();
        adventureData.setId("adv-1");
        adventureData.setTitle("The Demo");

        com.pdg.adventure.model.PictureData picture = new com.pdg.adventure.model.PictureData();
        picture.setId("pic-1");
        picture.setContentType("image/png");
        picture.setContent(new byte[] {1, 2, 3});
        java.util.HashMap<String, com.pdg.adventure.model.PictureData> pictures = new java.util.HashMap<>();
        pictures.put(picture.getId(), picture);
        adventureData.setPictureData(pictures);

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        view = new AdventureRunView(sessionFactory, accessService, new VariableProvider());
        UI.getCurrent().add(view);
    }

    // beforeEnter() now renders the opening room by directly executing a MovePlayerAction into
    // the session's current (start) location - not by submitting "look" through session.submit()
    // as before. Stubs that chain: session.getGameContext() -> a mocked GameContext whose
    // getCurrentLocation() returns a mocked Location with the given arrival description, and an
    // empty-message runArrivalProcesses() so MovePlayerAction.execute() doesn't NPE or append
    // anything extra.
    private void stubOpeningRoom(String description) {
        stubOpeningRoom(description, null);
    }

    private void stubOpeningRoom(String description, String pictureId) {
        com.pdg.adventure.server.location.Location startLocation =
                mock(com.pdg.adventure.server.location.Location.class);
        when(startLocation.getArrivalDescription())
                .thenReturn(new com.pdg.adventure.server.location.Location.LocationDescription(description, pictureId));
        when(gameContext.getCurrentLocation()).thenReturn(startLocation);
        when(gameContext.runArrivalProcesses()).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(gameContext.getCurrentPictureId()).thenReturn(pictureId);
        when(session.getGameContext()).thenReturn(gameContext);
        when(session.runBound(any())).thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private BeforeEnterEvent eventFor(String path) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getLocation()).thenReturn(new Location(path));
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        return event;
    }

    private void enterViaAuthorRoute() {
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenReturn(session);
        view.beforeEnter(eventFor("author/adventures/adv-1/test"));
    }

    @Test
    void beforeEnter_rendersTheOpeningRoomDescription() {
        stubOpeningRoom("A grand throne room.");

        enterViaAuthorRoute();

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
    }

    @Test
    void submittingAMessage_echoesThePlayersInput_thenAppendsTheEngineResponse() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        when(session.submit("go north")).thenReturn(new RunResult(List.of("You head north."), false));

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("go north");

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.", "go north", "You head north.");
    }

    @Test
    void multipleNarratorLinesFromOneTurn_arePooledIntoASingleMessageListItem() {
        // The opening room is now rendered directly via MovePlayerAction, not via session.submit(),
        // so the multi-line-pooling behavior (renderNarratorLines joining several lines into one
        // MessageListItem) is exercised here through a regular submitted turn instead.
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        when(session.submit("look")).thenReturn(
                new RunResult(List.of(SystemMessageKey.SM9.defaultText(), "a rusty key"), false));

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("look");

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.", "look",
                                  SystemMessageKey.SM9.defaultText() + "\na rusty key");
    }

    // Mirrors production order: beforeEnter runs before the view is attached, so the conflict dialog is
    // opened from onAttach.
    private AdventureRunView enterWhileAnotherRunIsActive() {
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenThrow(new RunAlreadyActiveException());
        AdventureRunView conflicted = new AdventureRunView(sessionFactory, accessService, new VariableProvider());
        conflicted.beforeEnter(eventFor("author/adventures/adv-1/test"));
        UI.getCurrent().add(conflicted);
        return conflicted;
    }

    @Test
    void beforeEnter_whenAnotherRunIsActive_disablesInputAndOffersTheTakeover() {
        AdventureRunView conflicted = enterWhileAnotherRunIsActive();

        assertThat(find(MessageInput.class, conflicted).single().isEnabled()).isFalse();
        ConfirmDialog dialog = find(ConfirmDialog.class).single();
        assertThat(test(dialog).getText()).contains("another tab");
    }

    @Test
    void confirmingTheTakeover_startsReplacingTheActiveRun_andRendersTheOpeningRoom() {
        stubOpeningRoom("A grand throne room.");
        when(sessionFactory.startReplacingActive(eq(adventureData), any(RunOwner.class))).thenReturn(session);
        AdventureRunView conflicted = enterWhileAnotherRunIsActive();

        test(find(ConfirmDialog.class).single()).confirm();

        verify(sessionFactory).startReplacingActive(eq(adventureData), any(RunOwner.class));
        MessageList messageList = find(MessageList.class, conflicted).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
        assertThat(find(MessageInput.class, conflicted).single().isEnabled()).isTrue();
    }

    @Test
    void theOpeningRoom_isRenderedThroughTheSessionsBoundEntryPoint() {
        stubOpeningRoom("A grand throne room.");

        enterViaAuthorRoute();

        verify(session).runBound(any());
    }

    @Test
    void detachingTheView_releasesItsRun() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();

        UI.getCurrent().remove(view);

        verify(sessionFactory).release(any(RunOwner.class));
    }

    @Test
    void enteringAgainOnTheSameViewInstance_releasesItsOwnRunBeforeStartingAnew() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();

        view.beforeEnter(eventFor("author/adventures/adv-1/test"));

        InOrder order = inOrder(sessionFactory);
        order.verify(sessionFactory).start(eq(adventureData), any(RunOwner.class));
        order.verify(sessionFactory).release(any(RunOwner.class));
        order.verify(sessionFactory).start(eq(adventureData), any(RunOwner.class));
    }

    @Test
    void aConflictOnAnAlreadyAttachedView_opensTheDialogImmediately() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenThrow(new RunAlreadyActiveException());

        view.beforeEnter(eventFor("author/adventures/adv-1/test"));

        assertThat(find(ConfirmDialog.class).exists()).isTrue();
        assertThat(find(MessageInput.class, view).single().isEnabled()).isFalse();
    }

    @Test
    void gameOver_releasesTheRun() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        when(session.submit("quit")).thenReturn(new RunResult(List.of(SystemMessageKey.SM14.defaultText()), true));

        test(find(MessageInput.class, view).single()).send("quit");

        verify(sessionFactory).release(any(RunOwner.class));
    }

    @Test
    void gameOver_disablesTheMessageInput() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        when(session.submit("quit")).thenReturn(new RunResult(List.of(SystemMessageKey.SM14.defaultText()), true));

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("quit");

        assertThat(messageInput.isEnabled()).isFalse();
    }

    @Test
    void beforeEnter_viaAuthorRoute_sessionCannotStart_forwardsToEditorWithAFlashMessage() {
        BeforeEnterEvent event = eventFor("author/adventures/adv-1/test");
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenThrow(new IllegalStateException("no locations"));

        view.beforeEnter(event);

        verify(event).forwardTo(eq(AdventureEditorView.class), any(RouteParameters.class));
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).contains("no locations");
    }

    @Test
    void beforeEnter_viaPlayerRoute_sessionCannotStart_forwardsToPlayerLibraryInstead() {
        BeforeEnterEvent event = eventFor("player/library/adv-1/run");
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenThrow(new IllegalStateException("no locations"));

        view.beforeEnter(event);

        verify(event).forwardTo(PlayerLibraryView.class);
    }

    @Test
    void beforeEnter_viaPlayerRoute_adventureNotFound_forwardsToPlayerLibraryNotAuthorMenu() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getLocation()).thenReturn(new Location("player/library/missing/run"));
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing")));
        when(accessService.findAdventureById(eq("missing"), any(UserData.class))).thenReturn(Optional.empty());

        view.beforeEnter(event);

        verify(event).forwardTo(PlayerLibraryView.class);
    }

    @Test
    void beforeEnter_viaPlayerRoute_rendersTheOpeningRoomDescription() {
        stubOpeningRoom("A grand throne room.");
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenReturn(session);

        view.beforeEnter(eventFor("player/library/adv-1/run"));

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
    }

    // The author-only "Run Adventure" (AdventuresMenuView) and "Test" (AdventureEditorView)
    // buttons both land on the same author/adventures/:id/test path - AdventureRunView.menuRunPath
    // adds ?from=menu to tell them apart, since the class-based navigate(AdventureRunView.class,
    // RouteParameters) that would normally distinguish two @Route/@RouteAlias registrations can't:
    // both routes share an identically-shaped :adventureId parameter, so Vaadin always resolves
    // outbound navigation to the primary @Route regardless of which button was actually clicked.
    @Test
    void beforeEnter_viaMenuRoute_sessionCannotStart_forwardsToAdventuresMenuNotEditor() {
        BeforeEnterEvent event = eventFor(AdventureRunView.menuRunPath("adv-1"));
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenThrow(new IllegalStateException("no locations"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void beforeEnter_viaMenuRoute_adventureNotFound_forwardsToAdventuresMenu() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getLocation()).thenReturn(new Location(AdventureRunView.menuRunPath("missing")));
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing")));
        when(accessService.findAdventureById(eq("missing"), any(UserData.class))).thenReturn(Optional.empty());

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void beforeEnter_viaMenuRoute_rendersTheOpeningRoomDescription() {
        stubOpeningRoom("A grand throne room.");
        when(sessionFactory.start(eq(adventureData), any(RunOwner.class))).thenReturn(session);

        view.beforeEnter(eventFor(AdventureRunView.menuRunPath("adv-1")));

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
    }

    @Test
    void beforeEnter_withAPictureOnTheStartLocation_showsIt() {
        stubOpeningRoom("A grand throne room.", "pic-1");

        enterViaAuthorRoute();

        com.vaadin.flow.component.html.Image image = find(com.vaadin.flow.component.html.Image.class, view).single();
        assertThat(image.getParent().orElseThrow().isVisible()).isTrue();
    }

    @Test
    void beforeEnter_withNoPictureOnTheStartLocation_hidesTheImageRegion() {
        stubOpeningRoom("A grand throne room.", null);

        enterViaAuthorRoute();

        assertThat(find(com.vaadin.flow.component.html.Image.class, view).exists()).isFalse();
    }

    @Test
    void aPictureAction_persistsThroughAnUnrelatedSubsequentCommand() {
        stubOpeningRoom("A grand throne room.", null);
        enterViaAuthorRoute();
        when(session.submit("look at chest")).thenReturn(new RunResult(List.of("A dusty chest."), false));
        when(gameContext.getCurrentPictureId()).thenReturn("pic-1");

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("look at chest");

        com.vaadin.flow.component.html.Image image = find(com.vaadin.flow.component.html.Image.class, view).single();
        assertThat(image.getParent().orElseThrow().isVisible()).isTrue();

        // An unrelated command follows - the mocked GameContext keeps returning "pic-1" since
        // nothing in this test resets it, exactly mirroring how the real GameContext leaves
        // currentPictureId untouched by a command that isn't a move, a look, or a PICTURE action.
        when(session.submit("take sword")).thenReturn(new RunResult(List.of("Taken."), false));
        test(messageInput).send("take sword");

        assertThat(image.getParent().orElseThrow().isVisible()).isTrue();
    }

    @Test
    void aFailedMove_leavesThePreviouslyShownPictureUnchanged() {
        stubOpeningRoom("A grand throne room.", "pic-1");
        enterViaAuthorRoute();
        when(session.submit("go north")).thenReturn(new RunResult(List.of("You can't go that way."), false));
        // GameContext mock keeps returning "pic-1" since nothing changed it - a failed move never
        // reaches the code path that would call setCurrentPictureId.

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("go north");

        com.vaadin.flow.component.html.Image image = find(com.vaadin.flow.component.html.Image.class, view).single();
        assertThat(image.getParent().orElseThrow().isVisible()).isTrue();
    }

    @Test
    void aPictureThatNoLongerExistsInTheAdventure_doesNotCrash_andHidesTheImageRegion() {
        stubOpeningRoom("A grand throne room.", "missing-picture-id");

        enterViaAuthorRoute();

        assertThat(find(com.vaadin.flow.component.html.Image.class, view).exists()).isFalse();
    }

    @Test
    void theAdventuresFont_isAppliedToTheGameTextAndTheInput() {
        stubOpeningRoom("A grand throne room.");
        adventureData.setFont(com.pdg.adventure.model.AdventureFont.CINZEL);

        enterViaAuthorRoute();

        String expected = com.pdg.adventure.model.AdventureFont.CINZEL.cssFontFamily().orElseThrow();
        assertThat(find(MessageList.class, view).single().getStyle().get("--lumo-font-family")).isEqualTo(expected);
        assertThat(find(MessageInput.class, view).single().getStyle().get("--lumo-font-family")).isEqualTo(expected);
    }

    @Test
    void theDefaultFont_leavesTheApplicationFontAlone() {
        stubOpeningRoom("A grand throne room.");

        enterViaAuthorRoute();

        assertThat(find(MessageList.class, view).single().getStyle().get("--lumo-font-family")).isNull();
        assertThat(find(MessageInput.class, view).single().getStyle().get("--lumo-font-family")).isNull();
    }

    // A message with its own font arrives inside the turn's text, wrapped by FontMarkup (see MessageAction):
    // the view shows each font run as a message of its own and tags it with the font's CSS class.
    @Test
    void aMessageWithItsOwnFont_isShownAsASeparateMessageCarryingThatFontsClass() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        String nl = System.lineSeparator();
        String turn = "You read the note:" + nl
                      + com.pdg.adventure.server.engine.FontMarkup.wrap(
                              "Meet me at midnight.", com.pdg.adventure.model.AdventureFont.SPECIAL_ELITE)
                      + nl + "You put it down.";
        when(session.submit("read note")).thenReturn(new RunResult(List.of(turn), false));

        test(find(MessageInput.class, view).single()).send("read note");

        List<MessageListItem> items = test(find(MessageList.class, view).single()).getMessages();
        assertThat(items).extracting(MessageListItem::getText).containsExactly(
                "A grand throne room.", "read note", "You read the note:", "Meet me at midnight.", "You put it down.");
        assertThat(items.get(3).hasClassName("run-font-special-elite")).isTrue();
        assertThat(items).filteredOn(item -> !item.getText().equals("Meet me at midnight."))
                         .noneMatch(item -> item.hasClassName("run-font-special-elite"));
    }

    @Test
    void theOpeningRoomIsSplitByFontToo() {
        stubOpeningRoom(com.pdg.adventure.server.engine.FontMarkup.wrap(
                "A faded poster.", com.pdg.adventure.model.AdventureFont.CINZEL));

        enterViaAuthorRoute();

        List<MessageListItem> items = test(find(MessageList.class, view).single()).getMessages();
        assertThat(items).extracting(MessageListItem::getText).containsExactly("A faded poster.");
        assertThat(items.getFirst().hasClassName("run-font-cinzel")).isTrue();
    }

    @Test
    void textWithoutAnyFontMarkerIsShownExactlyAsBefore_withNoFontClass() {
        stubOpeningRoom("A grand throne room.");
        enterViaAuthorRoute();
        when(session.submit("look")).thenReturn(new RunResult(List.of("line one", "line two"), false));

        test(find(MessageInput.class, view).single()).send("look");

        List<MessageListItem> items = test(find(MessageList.class, view).single()).getMessages();
        assertThat(items.getLast().getText()).isEqualTo("line one\nline two");
        assertThat(items).noneMatch(item -> java.util.Arrays.stream(com.pdg.adventure.model.AdventureFont.values())
                .anyMatch(font -> font.cssClassName().map(item::hasClassName).orElse(false)));
    }
}
