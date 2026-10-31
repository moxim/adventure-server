package com.pdg.adventure.view.location;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.direction.DirectionBuilder;
import com.pdg.adventure.view.direction.DirectionEditorView;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Connects two locations of the world map. Lists the directions that already lead from the source to the target
 * (each can be edited in the {@link DirectionEditorView}) and lets the author add a direction, optionally with the
 * opposite direction back. The author chooses all words from the adventure's vocabulary; there are no built-in
 * compass words.
 */
public class ConnectLocationsDialog extends Dialog {

    private record Warning(String header, String text) {
    }

    private final transient AdventureData adventure;
    private final transient AdventureService service;
    private final transient LocationData source;
    private final transient LocationData target;
    private final transient Runnable onDone;

    private final VocabularyPickerField verb = new VocabularyPickerField("Verb", "The action needed to follow this direction.");
    private final VocabularyPickerField adjective = new VocabularyPickerField("Adjective", "The qualifier for this direction.");
    private final VocabularyPickerField noun = new VocabularyPickerField("Noun", "A descriptive noun for this direction.");
    private final VocabularyPickerField oppositeVerb = new VocabularyPickerField("Opposite verb", "The action needed to come back.");
    private final VocabularyPickerField oppositeAdjective = new VocabularyPickerField("Opposite adjective", "The qualifier for the way back.");
    private final VocabularyPickerField oppositeNoun = new VocabularyPickerField("Opposite noun", "A descriptive noun for the way back.");
    private final Checkbox opposite = new Checkbox("Create the opposite direction?");
    private final VerticalLayout oppositeFields = new VerticalLayout(oppositeVerb, oppositeAdjective, oppositeNoun);
    private final VerticalLayout form = new VerticalLayout();
    private final Button save = new Button("Save", _ -> onSave());

    public ConnectLocationsDialog(AdventureData anAdventure, AdventureService aService, LocationData aSource,
                                  LocationData aTarget, Runnable anOnDone) {
        adventure = anAdventure;
        service = aService;
        source = aSource;
        target = aTarget;
        onDone = anOnDone;

        setHeaderTitle("Connect " + name(source) + " to " + name(target));

        populatePickers();
        oppositeFields.setPadding(false);
        oppositeFields.setVisible(false);
        form.setPadding(false);
        form.add(verb, adjective, noun, opposite, oppositeFields);
        if (adventure.getVocabularyData().getWords(Word.Type.VERB).isEmpty()) {
            form.addComponentAsFirst(new Paragraph(
                    "The vocabulary has no verb yet. Add a verb to the adventure's vocabulary first."));
        }

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        List<DirectionData> existing = LocationMapPlacement.directionsTo(source, target.getId());
        if (existing.isEmpty()) {
            form.setVisible(true);
        } else {
            form.setVisible(false);
            existing.forEach(direction -> content.add(new HorizontalLayout(
                    new Span(describe(direction)), new Button("Edit", _ -> editDirection(direction)))));
            Button addAnother = new Button("Add another direction");
            addAnother.addClickListener(_ -> {
                form.setVisible(true);
                addAnother.setVisible(false);
            });
            content.add(addAnother);
        }
        content.add(form);
        add(content);

        opposite.addValueChangeListener(event -> {
            oppositeFields.setVisible(event.getValue());
            updateSave();
        });
        verb.addValueChangeListener(_ -> updateSave());
        oppositeVerb.addValueChangeListener(_ -> updateSave());
        save.setEnabled(false);
        getFooter().add(new Button("Cancel", _ -> close()), save);
    }

    /** Opens the direction in the Direction editor, a separate page; its Back button does not return to the map. */
    void editDirection(DirectionData aDirection) {
        close();
        UI.getCurrent().navigate(DirectionEditorView.class, new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventure.getId()),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), source.getId()),
                new RouteParam(RouteIds.DIRECTION_ID.getValue(), aDirection.getId())));
    }

    private void populatePickers() {
        for (VocabularyPickerField picker : List.of(verb, oppositeVerb)) {
            picker.populate(roots(Word.Type.VERB));
        }
        for (VocabularyPickerField picker : List.of(adjective, oppositeAdjective)) {
            picker.populate(roots(Word.Type.ADJECTIVE));
        }
        for (VocabularyPickerField picker : List.of(noun, oppositeNoun)) {
            picker.populate(roots(Word.Type.NOUN));
        }
    }

    /** The parser resolves a typed synonym to its root, so a direction must hold the root to ever be followed. */
    private List<Word> roots(Word.Type aType) {
        return adventure.getVocabularyData().getWords(aType).stream().filter(word -> word.getSynonym() == null)
                        .toList();
    }

    private void updateSave() {
        save.setEnabled(verb.getValue() != null && (!opposite.getValue() || oppositeVerb.getValue() != null));
    }

    private void onSave() {
        List<Warning> warnings = new ArrayList<>();
        if (source.equals(target)) {
            warnings.add(new Warning("Dangerous Destination",
                                     "The selected destination is the same as the current location. This will trap "
                                     + "the player if they follow this direction. Do you want to proceed?"));
        }
        LocationMapPlacement.findSameWords(source, verb.getValue(), adjective.getValue(), noun.getValue())
                            .ifPresent(same -> warnings.add(sameWordsWarning(source, same)));
        if (opposite.getValue()) {
            LocationMapPlacement.findSameWords(target, oppositeVerb.getValue(), oppositeAdjective.getValue(),
                                               oppositeNoun.getValue())
                                .ifPresent(same -> warnings.add(sameWordsWarning(target, same)));
        }
        confirmAll(warnings, this::create);
    }

    private static Warning sameWordsWarning(LocationData anOwner, DirectionData aSameWords) {
        return new Warning("Same words", name(anOwner) + " already has the direction \""
                                         + describe(aSameWords) + "\". Do you want to add the new one anyway?");
    }

    /** Asks the warnings one after the other; only when the author confirmed all of them does {@code proceed} run. */
    private void confirmAll(List<Warning> aWarnings, Runnable aProceed) {
        if (aWarnings.isEmpty()) {
            aProceed.run();
            return;
        }
        Warning warning = aWarnings.getFirst();
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(warning.header());
        confirm.setText(warning.text());
        confirm.setCancelable(true);
        confirm.setConfirmText("Proceed");
        confirm.addConfirmListener(_ -> confirmAll(aWarnings.subList(1, aWarnings.size()), aProceed));
        confirm.open();
    }

    private void create() {
        Set<LocationData> owners = new LinkedHashSet<>();
        DirectionBuilder.create(source, target.getId(), verb.getValue(), adjective.getValue(), noun.getValue());
        owners.add(source);
        if (opposite.getValue()) {
            DirectionBuilder.create(target, source.getId(), oppositeVerb.getValue(), oppositeAdjective.getValue(),
                                    oppositeNoun.getValue());
            owners.add(target);
        }
        LocationMapSaver.saveAll(service, owners);
        close();
        onDone.run();
    }

    private static String name(LocationData aLocation) {
        return ViewSupporter.formatDescription(aLocation).strip();
    }

    private static String describe(DirectionData aDirection) {
        return ViewSupporter.formatDescription(aDirection.getCommandData().getCommandDescription());
    }
}
