package com.pdg.adventure.views.components;

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
    @Getter
    Button cancel = new Button("Cancel", e -> {
        reset.clickInClient();
        back.clickInClient();
    });

    public ResetBackSaveView() {
        getContent().add(cancel, reset, back, save);
    }

}
