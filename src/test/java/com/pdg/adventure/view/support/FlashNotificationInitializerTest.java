package com.pdg.adventure.view.support;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.UIInitListener;
import com.vaadin.flow.server.VaadinService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlashNotificationInitializerTest extends BrowserlessTest {

    @Test
    void serviceInit_registersAfterNavigationListenerThatShowsPendingFlash() {
        VaadinService service = mock(VaadinService.class);
        ServiceInitEvent initEvent = mock(ServiceInitEvent.class);
        when(initEvent.getSource()).thenReturn(service);

        new FlashNotificationInitializer().serviceInit(initEvent);

        ArgumentCaptor<UIInitListener> uiInitCaptor = ArgumentCaptor.forClass(UIInitListener.class);
        verify(service).addUIInitListener(uiInitCaptor.capture());

        UI ui = mock(UI.class);
        UIInitEvent uiInitEvent = mock(UIInitEvent.class);
        when(uiInitEvent.getUI()).thenReturn(ui);
        uiInitCaptor.getValue().uiInit(uiInitEvent);

        ArgumentCaptor<AfterNavigationListener> navCaptor =
                ArgumentCaptor.forClass(AfterNavigationListener.class);
        verify(ui).addAfterNavigationListener(navCaptor.capture());

        FlashNotifier.flash("Adventure not found or access denied: missing");
        navCaptor.getValue().afterNavigation(mock(AfterNavigationEvent.class));

        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
