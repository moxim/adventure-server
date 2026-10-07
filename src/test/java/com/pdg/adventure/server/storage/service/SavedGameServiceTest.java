package com.pdg.adventure.server.storage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.GameSnapshotData;
import com.pdg.adventure.model.SavedGameData;
import com.pdg.adventure.server.storage.repository.SavedGameRepository;

class SavedGameServiceTest {

    private SavedGameRepository repository;
    private SavedGameService service;

    @BeforeEach
    void setUp() {
        repository = mock(SavedGameRepository.class);
        service = new SavedGameService(repository);
    }

    private static SavedGameData saved(int slot) {
        SavedGameData data = new SavedGameData();
        data.setUserId("u1");
        data.setAdventureId("a1");
        data.setSlot(slot);
        return data;
    }

    private void givenSlots(int... slots) {
        List<SavedGameData> existing = new ArrayList<>();
        for (int slot : slots) {
            existing.add(saved(slot));
        }
        when(repository.findByUserIdAndAdventureId("u1", "a1")).thenReturn(existing);
    }

    @Test
    void freeSlot_isTheLowestUnusedSlot() {
        givenSlots(1, 2, 4);

        assertThat(service.freeSlot("u1", "a1")).hasValue(3);
    }

    @Test
    void freeSlot_isEmptyWhenAllTenAreTaken() {
        givenSlots(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        assertThat(service.freeSlot("u1", "a1")).isEmpty();
    }

    @Test
    void save_upsertsUnderTheDeterministicId() {
        GameSnapshotData snapshot = new GameSnapshotData();
        Instant now = Instant.parse("2026-10-07T11:43:45Z");
        when(repository.save(any(SavedGameData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SavedGameData result = service.save("u1", "a1", 3, "1.0.0", snapshot, now);

        assertThat(result.getId()).isEqualTo("u1:a1:3");
        assertThat(result.getSlot()).isEqualTo(3);
        assertThat(result.getSavedAt()).isEqualTo(now);
        assertThat(result.getBuilderVersion()).isEqualTo("1.0.0");
        assertThat(result.getSnapshot()).isSameAs(snapshot);
        verify(repository).save(result);
    }

    @Test
    void save_rejectsSlotsOutsideOneToTen() {
        assertThatThrownBy(() -> service.save("u1", "a1", 0, null, new GameSnapshotData(), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.save("u1", "a1", 11, null, new GameSnapshotData(), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void list_isOrderedBySlot() {
        givenSlots(5, 2, 9);

        assertThat(service.list("u1", "a1")).extracting(SavedGameData::getSlot).containsExactly(2, 5, 9);
    }

    @Test
    void find_looksUpTheDeterministicId() {
        SavedGameData data = saved(4);
        when(repository.findById("u1:a1:4")).thenReturn(Optional.of(data));

        assertThat(service.find("u1", "a1", 4)).containsSame(data);
        assertThat(service.find("u1", "a1", 5)).isEmpty();
    }

    @Test
    void parseSlot_acceptsOnlyOneToTen() {
        assertThat(SavedGameService.parseSlot("1")).hasValue(1);
        assertThat(SavedGameService.parseSlot("10")).hasValue(10);
        assertThat(SavedGameService.parseSlot("0")).isEmpty();
        assertThat(SavedGameService.parseSlot("11")).isEmpty();
        assertThat(SavedGameService.parseSlot("lamp")).isEmpty();
        assertThat(SavedGameService.parseSlot("")).isEmpty();
        assertThat(SavedGameService.parseSlot(null)).isEmpty();
    }

    @Test
    void label_isTitleDashDateAndTime() {
        Instant savedAt = Instant.parse("2026-10-07T11:43:45Z");

        assertThat(SavedGameService.label("The Demo", savedAt, ZoneId.of("UTC")))
                .isEqualTo("The Demo - 2026-10-07 11:43:45");
    }
}
