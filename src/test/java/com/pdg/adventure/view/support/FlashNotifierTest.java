package com.pdg.adventure.view.support;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.notification.Notification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlashNotifierTest extends BrowserlessTest {

    @Test
    void flash_doesNotShowImmediately() {
        FlashNotifier.flash("Adventure not found or access denied: missing");

        assertThat(find(Notification.class).all()).isEmpty();
    }

    @Test
    void flash_thenShowPending_showsErrorNotificationOnce() {
        FlashNotifier.flash("Adventure not found or access denied: missing");

        FlashNotifier.showPending();

        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void showPending_consumesTheMessage_secondCallShowsNothingNew() {
        FlashNotifier.flash("Location not found or access denied: missing");

        FlashNotifier.showPending();
        FlashNotifier.showPending();

        assertThat(find(Notification.class).all()).hasSize(1);
    }

    @Test
    void showPending_withNothingPending_showsNothing() {
        FlashNotifier.showPending();

        assertThat(find(Notification.class).all()).isEmpty();
    }

    @Test
    void flash_twiceBeforeShowPending_lastMessageWins() {
        FlashNotifier.flash("first");
        FlashNotifier.flash("second");

        FlashNotifier.showPending();

        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("second");
    }
}
