package com.pdg.adventure.view.support;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Starts {@link HistoryNavigation}'s history recording in every browser tab (UI).
 * Registered automatically by Spring (see the Vaadin service-init-listener docs).
 */
@Component
public class HistoryNavigationInitializer implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInitEvent -> HistoryNavigation.installTrail(uiInitEvent.getUI()));
    }
}
