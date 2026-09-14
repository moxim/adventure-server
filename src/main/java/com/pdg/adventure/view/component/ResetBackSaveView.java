package com.pdg.adventure.view.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;


public class ResetBackSaveView extends Composite<HorizontalLayout> {
    @Getter
    Button reset = new Button("Reset");
    @Getter
    Button back = new Button("Back");
    @Getter
    Button save = new Button("Save");
    // Cancel is "leave without saving" - it must go through Back's own click listener (which
    // triggers real navigation, and with it each view's BeforeLeaveObserver unsaved-changes
    // guard) rather than resetting the form first. Resetting first would clear the binder's
    // dirty flag before that guard runs, silently skipping the "uncommitted changes" prompt
    // that clicking Back alone still shows for the exact same destination.
    @Getter
    Button cancel = new Button("Cancel", _ -> back.click());

    public ResetBackSaveView() {
        getContent().add(cancel, reset, back, save);
    }

}
