package com.pdg.adventure.view.support;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinSession;

/**
 * Session-scoped flash messages for error notifications that must survive a navigation.
 *
 * <p>A {@code Notification} opened during {@code beforeEnter} does not survive an
 * {@code event.forwardTo(...)} on a real (cold-load) navigation — the forwarding view's UI is
 * discarded before the client renders it, so the user lands on the fallback page with no
 * explanation. Queueing the message in the {@link VaadinSession} and showing it from the
 * {@code AfterNavigationListener} registered by {@link FlashNotificationInitializer} displays it
 * once the navigation the forward triggered has completed.
 */
public final class FlashNotifier {

    static final String FLASH_ATTRIBUTE = FlashNotifier.class.getName() + ".pendingError";

    private FlashNotifier() {
    }

    /**
     * Queues an error message to be shown after the next completed navigation. Without an active
     * {@link VaadinSession} (e.g. a background thread) the message is shown immediately instead
     * of being silently dropped.
     */
    public static void flash(String message) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            show(message);
            return;
        }
        session.setAttribute(FLASH_ATTRIBUTE, message);
    }

    /** Shows and clears the queued message, if any. */
    public static void showPending() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return;
        }
        Object pending = session.getAttribute(FLASH_ATTRIBUTE);
        if (pending != null) {
            session.setAttribute(FLASH_ATTRIBUTE, null);
            show(pending.toString());
        }
    }

    private static void show(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
