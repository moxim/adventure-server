package com.pdg.adventure.view.adventure;


import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.router.BeforeLeaveEvent;

import com.pdg.adventure.view.component.AdventureAppLayout;

/**
 * The main view is a top-level placeholder for other view.
 */
public class AdventuresMainLayout extends AdventureAppLayout {

    public AdventuresMainLayout() {
        String title = "Adventure Builder";

        createHeader(title);
        createDrawer(title);

        setPrimarySection(Section.NAVBAR);
    }


    public static void checkIfUserWantsToLeavePage(BeforeLeaveEvent anEvent, boolean pageHasChanges) {
        if (pageHasChanges) {
            BeforeLeaveEvent.ContinueNavigationAction action = anEvent.postpone();
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setText("You have uncommitted changes! Are you sure you want to leave?");
            confirmDialog.setCancelable(true);
            confirmDialog.addConfirmListener(_ -> action.proceed());
            confirmDialog.open();
        }
    }
}
