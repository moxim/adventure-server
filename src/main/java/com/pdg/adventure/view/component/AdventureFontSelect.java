package com.pdg.adventure.view.component;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import com.pdg.adventure.model.AdventureFont;

/**
 * A select for an {@link AdventureFont} that draws each entry in its own font, so the author sees what they are
 * choosing. The preview only works for fonts the page has loaded (adventure-fonts.css); an unloaded one shows its
 * fallback. There is no empty selection: {@link AdventureFont#DEFAULT} is the "no override" entry, and the caller
 * says what that means in its context (the adventure editor: the application font; the message editor: the
 * adventure's own font).
 */
public class AdventureFontSelect extends Select<AdventureFont> {

    public AdventureFontSelect(String aLabel, String aDefaultEntryLabel) {
        setLabel(aLabel);
        setItems(AdventureFont.values());
        setEmptySelectionAllowed(false);
        setItemLabelGenerator(font -> font == AdventureFont.DEFAULT ? aDefaultEntryLabel : font.label());
        setRenderer(new ComponentRenderer<>(font -> {
            Span preview = new Span(getItemLabelGenerator().apply(font));
            font.cssFontFamily().ifPresent(family -> preview.getStyle().set("font-family", family));
            return preview;
        }));
    }
}
