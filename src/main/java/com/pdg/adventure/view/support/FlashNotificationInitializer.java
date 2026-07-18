package com.pdg.adventure.view.support;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Shows any queued {@link FlashNotifier} message once a navigation completes, on every UI.
 * Registered automatically by Spring (see the Vaadin service-init-listener docs).
 */
@Component
public class FlashNotificationInitializer implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(
                uiInitEvent -> uiInitEvent.getUI().addAfterNavigationListener(_ -> FlashNotifier.showPending()));
    }
}
