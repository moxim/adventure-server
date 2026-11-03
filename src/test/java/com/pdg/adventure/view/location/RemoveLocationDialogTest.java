package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.direction.DirectionBuilder;

class RemoveLocationDialogTest extends BrowserlessTest {

    private static final String LEAVING = "Directions leading away from here";
    private static final String ARRIVING = "Directions leading here";

    private final Word go = new Word("go", Word.Type.VERB);
    private final Word enter = new Word("enter", Word.Type.VERB);
    private final Word north = new Word("north", Word.Type.NOUN);
    private final Word door = new Word("door", Word.Type.NOUN);

    private AdventureService service;
    private AdventureData adventure;
    private LocationData a;
    private LocationData x;
    private AtomicBoolean done;
    private RemoveLocationDialog dialog;

    @BeforeEach
    void setUp() {
        service = mock(AdventureService.class);
        adventure = new AdventureData();
        a = location("a", "attic", 0, 0);
        x = location("x", "cellar", 5, 5);
        done = new AtomicBoolean();
    }

    private LocationData location(String anId, String aNoun, Integer aColumn, Integer aRow) {
        LocationData location = new LocationData();
        location.setId(anId);
        location.getDescriptionData().setNoun(new Word(aNoun, Word.Type.NOUN));
        location.setMapColumn(aColumn);
        location.setMapRow(aRow);
        adventure.getLocationData().put(anId, location);
        return location;
    }

    private void open() {
        dialog = new RemoveLocationDialog(adventure, service, x, () -> done.set(true));
        dialog.open();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CheckboxGroup<Object> group(String aLabel) {
        return (CheckboxGroup<Object>) find(CheckboxGroup.class, dialog).all().stream()
                .filter(candidate -> aLabel.equals(candidate.getLabel())).findFirst().orElseThrow();
    }

    private boolean hasGroup(String aLabel) {
        return find(CheckboxGroup.class, dialog).all().stream().anyMatch(group -> aLabel.equals(group.getLabel()));
    }

    private Button button(String aText) {
        return find(Button.class, dialog).withText(aText).single();
    }

    @Test
    void listsTheDirectionsLeavingAndArrivingAndTicksNone() {
        DirectionBuilder.create(x, "a", go, null, north);
        DirectionBuilder.create(a, "x", enter, null, door);

        open();

        assertThat(group(LEAVING).getListDataView().getItems()).hasSize(1);
        assertThat(group(ARRIVING).getListDataView().getItems()).hasSize(1);
        assertThat(group(LEAVING).getValue()).isEmpty();
        assertThat(group(ARRIVING).getValue()).isEmpty();
    }

    @Test
    void anArrivingDirectionIsLabelledWithTheLocationItComesFrom() {
        DirectionBuilder.create(a, "x", enter, null, door);

        open();

        Object entry = group(ARRIVING).getListDataView().getItems().findFirst().orElseThrow();
        assertThat(find(CheckboxGroup.class, dialog).all().getFirst().getItemLabelGenerator()
                           .apply(entry)).startsWith("attic");
    }

    @Test
    void aSectionWithoutDirectionsIsNotShown() {
        DirectionBuilder.create(x, "a", go, null, north);

        open();

        assertThat(hasGroup(LEAVING)).isTrue();
        assertThat(hasGroup(ARRIVING)).isFalse();
    }

    @Test
    void removeWithNothingTickedOnlyTakesTheLocationOffTheMap() {
        DirectionData out = DirectionBuilder.create(x, "a", go, null, north);
        DirectionData in = DirectionBuilder.create(a, "x", enter, null, door);
        open();

        test(button("Remove")).click();

        assertThat(x.getMapColumn()).isNull();
        assertThat(x.getMapRow()).isNull();
        assertThat(x.getDirectionsData()).containsExactly(out);
        assertThat(a.getDirectionsData()).containsExactly(in);
        verify(service).saveLocationData(x);
        verifyNoMoreInteractions(service);
        assertThat(done).isTrue();
    }

    @Test
    void removeTakesOnlyTheTickedDirectionsAlong() {
        DirectionData out = DirectionBuilder.create(x, "a", go, null, north);
        DirectionBuilder.create(x, "a", enter, null, door);
        DirectionData in = DirectionBuilder.create(a, "x", enter, null, door);
        open();
        group(LEAVING).select(out);
        group(ARRIVING).select(group(ARRIVING).getListDataView().getItems().findFirst().orElseThrow());

        test(button("Remove")).click();

        assertThat(x.getDirectionsData()).hasSize(1).doesNotContain(out);
        assertThat(a.getDirectionsData()).doesNotContain(in).isEmpty();
        verify(service).saveLocationData(x);
        verify(service).saveLocationData(a);
        assertThat(done).isTrue();
    }

    @Test
    void cancelChangesAndSavesNothing() {
        DirectionBuilder.create(x, "a", go, null, north);
        open();

        test(button("Cancel")).click();

        assertThat(x.getMapColumn()).isEqualTo(5);
        verifyNoMoreInteractions(service);
        assertThat(done).isFalse();
    }
}
