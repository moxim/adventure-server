package com.pdg.adventure.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

@Tag("paper-slider")
@JsModule("@polymer/paper-slider/paper-slider.js")
public class PaperSlider extends Component //implements Field<PaperSlider, Integer>
{

    public PaperSlider() {
//        Field.initSingleProperty(this, 0, "value");
    }

    public void setMax(int aMaxValue) {
        getElement().setProperty("max", aMaxValue);
    }
}
