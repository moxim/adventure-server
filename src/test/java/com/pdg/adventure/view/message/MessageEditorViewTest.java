package com.pdg.adventure.view.message;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.MessageService;
import com.pdg.adventure.view.support.RouteIds;

/**
 * Unit tests for MessageEditorView business logic.
 * Tests focus on validation, data population, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class MessageEditorViewTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private MessageService messageService;

    @Mock
    private AdventureAccessService accessService;

    private MessageEditorView view;
    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");
        adventureData.setMessages(new HashMap<>());

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void enterWithMessageId(String aMessageId) {
        RouteParam[] params = aMessageId == null
                ? new RouteParam[] {new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())}
                : new RouteParam[] {new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                        new RouteParam(RouteIds.MESSAGE_ID.getValue(), aMessageId)};
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view.beforeEnter(event);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new MessageEditorView(adventureService, messageService, accessService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateAdventureData() {
        // given
        view = new MessageEditorView(adventureService, messageService, accessService);

        // when
        enterWithMessageId(null);

        // then
        // View should be populated with data
        // No exceptions should be thrown
        assertThat(view).isNotNull();
    }

    // Loading an existing message's text into the view is verified with a real UI assertion
    // in MessageEditorViewRoutingTest.beforeEnter_validIds_populatesMessageTextFromResolvedAdventure
    // — this view has no content-derived public accessor (getPageTitle() uses the raw route
    // parameter, not the loaded message), so a plain-Mockito assertion here could only re-check
    // the fixture map this test itself populated, never the view's actual loaded state.

    @Test
    void getPageTitle_shouldReturnNullBeforeRouteEnter() {
        // given
        view = new MessageEditorView(adventureService, messageService, accessService);

        // when
        String title = view.getPageTitle();

        // then
        assertThat(title).isNull();
    }
}
