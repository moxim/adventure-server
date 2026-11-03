package com.pdg.adventure.view.support;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.node.BooleanNode;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.view.about.AboutView;

/**
 * The Back buttons step back in the browser history instead of adding another entry, but only when the browser
 * says the previous entry is the view being returned to; otherwise (and always with unsaved changes) they
 * navigate exactly as they always did. The browser's answer is played by completing the pending JS invocation.
 */
class HistoryNavigationTest extends BrowserlessTest {

    private static final String STEP_BACK = "stepBackTo";

    @BeforeEach
    void loggedInAuthor() {
        UserData user = new UserData();
        user.setUsername("test-user");
        user.setRoles(Set.of(Role.AUTHOR));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static List<PendingJavaScriptInvocation> stepBackRequests() {
        return UI.getCurrent().getInternals().dumpPendingJavaScriptInvocations().stream()
                 .filter(invocation -> invocation.getInvocation().getExpression().contains(STEP_BACK))
                 .toList();
    }

    private static boolean onAboutView() {
        return UI.getCurrent().getInternals().getActiveViewLocation().getPath().equals("about");
    }

    @Test
    void withUnsavedChanges_navigatesStraightAway_withoutAskingTheBrowser() {
        HistoryNavigation.navigateUp(AboutView.class, true);

        assertThat(onAboutView()).isTrue();
        assertThat(stepBackRequests()).isEmpty();
    }

    @Test
    void withoutUnsavedChanges_asksTheBrowserWhetherThePreviousEntryIsTheParent_andDoesNotNavigateYet() {
        HistoryNavigation.navigateUp(AboutView.class);

        List<PendingJavaScriptInvocation> requests = stepBackRequests();
        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getInvocation().getParameters()).containsExactly("about");
        assertThat(onAboutView()).isFalse();
    }

    @Test
    void whenTheBrowserSteppedBack_noSecondNavigationIsAdded() {
        HistoryNavigation.navigateUp(AboutView.class, false);

        stepBackRequests().getFirst().complete(BooleanNode.TRUE);

        assertThat(onAboutView()).isFalse();
    }

    @Test
    void whenThePreviousEntryIsNotTheParent_fallsBackToPlainNavigation() {
        HistoryNavigation.navigateUp(AboutView.class, false);

        stepBackRequests().getFirst().complete(BooleanNode.FALSE);

        assertThat(onAboutView()).isTrue();
    }

    @Test
    void whenTheBrowserCannotAnswer_fallsBackToPlainNavigation() {
        HistoryNavigation.navigateUp(AboutView.class, false);

        stepBackRequests().getFirst().completeExceptionally(BooleanNode.FALSE);

        assertThat(onAboutView()).isTrue();
    }

    @Test
    void initializer_startsTheHistoryRecordingOnEveryNewUi() {
        VaadinService service = mock(VaadinService.class);
        ServiceInitEvent initEvent = mock(ServiceInitEvent.class);
        when(initEvent.getSource()).thenReturn(service);

        new HistoryNavigationInitializer().serviceInit(initEvent);

        ArgumentCaptor<UIInitListener> uiInitCaptor = ArgumentCaptor.forClass(UIInitListener.class);
        verify(service).addUIInitListener(uiInitCaptor.capture());
        UIInitEvent uiInitEvent = mock(UIInitEvent.class);
        when(uiInitEvent.getUI()).thenReturn(UI.getCurrent());
        uiInitCaptor.getValue().uiInit(uiInitEvent);

        List<PendingJavaScriptInvocation> recording = UI.getCurrent().getInternals()
                .dumpPendingJavaScriptInvocations().stream()
                .filter(invocation -> invocation.getInvocation().getExpression().contains("adventure-nav-trail"))
                .toList();
        assertThat(recording).hasSize(1);
    }
}
