package com.pdg.adventure.server.storage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.SoundContentData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.repository.SoundContentRepository;
import com.pdg.adventure.server.storage.repository.SoundRepository;

class SoundServiceTest {

    private SoundRepository soundRepository;
    private SoundContentRepository contentRepository;
    private CascadeDeleteHelper cascadeDeleteHelper;
    private SoundService service;

    @BeforeEach
    void setUp() {
        soundRepository = mock(SoundRepository.class);
        contentRepository = mock(SoundContentRepository.class);
        cascadeDeleteHelper = mock(CascadeDeleteHelper.class);
        service = new SoundService(soundRepository, contentRepository, cascadeDeleteHelper);
    }

    private static SoundData sound(String id, String name) {
        SoundData sound = new SoundData();
        sound.setId(id);
        sound.setName(name);
        return sound;
    }

    @Test
    void save_withNewContent_savesContentBeforeSoundAndLinksIt() {
        SoundData sound = sound("s1", "pirate_bay");
        byte[] bytes = {1, 2, 3, 4};
        when(contentRepository.save(any(SoundContentData.class))).thenAnswer(call -> call.getArgument(0));

        service.save(sound, bytes);

        InOrder order = inOrder(contentRepository, soundRepository);
        ArgumentCaptor<SoundContentData> content = ArgumentCaptor.forClass(SoundContentData.class);
        order.verify(contentRepository).save(content.capture());
        order.verify(soundRepository).save(sound);
        assertThat(content.getValue().getContent()).containsExactly(bytes);
        assertThat(content.getValue().getId()).isNotBlank();
        assertThat(sound.getContent()).isSameAs(content.getValue());
        assertThat(sound.getSizeBytes()).isEqualTo(4);
        assertThat(sound.getContentType()).isEqualTo("audio/mpeg");
    }

    @Test
    void save_withNullContent_leavesTheStoredContentUntouched() {
        SoundData sound = sound("s1", "pirate_bay");

        service.save(sound, null);

        verify(contentRepository, never()).save(any());
        verify(soundRepository).save(sound);
    }

    @Test
    void save_replacingContent_reusesTheExistingContentDocumentId() {
        SoundData sound = sound("s1", "pirate_bay");
        SoundContentData old = new SoundContentData();
        old.setId("c-old");
        old.setContent(new byte[] {9});
        sound.setContent(old);
        when(contentRepository.save(any(SoundContentData.class))).thenAnswer(call -> call.getArgument(0));

        service.save(sound, new byte[] {1, 2});

        ArgumentCaptor<SoundContentData> content = ArgumentCaptor.forClass(SoundContentData.class);
        verify(contentRepository).save(content.capture());
        assertThat(content.getValue().getId()).isEqualTo("c-old");
        assertThat(content.getValue().getContent()).containsExactly(1, 2);
    }

    @Test
    void delete_cascadesThenDeletesTheSound() {
        SoundData sound = sound("s1", "pirate_bay");
        when(soundRepository.findById("s1")).thenReturn(Optional.of(sound));

        service.delete("s1");

        InOrder order = inOrder(cascadeDeleteHelper, soundRepository);
        order.verify(cascadeDeleteHelper).cascadeDelete(sound);
        order.verify(soundRepository).delete(sound);
    }

    @Test
    void delete_ofAnUnknownSound_doesNothing() {
        when(soundRepository.findById("nope")).thenReturn(Optional.empty());

        service.delete("nope");

        verify(soundRepository, never()).delete(any());
        verify(cascadeDeleteHelper, never()).cascadeDelete(any());
    }

    @Test
    void isNameTaken_ignoresCaseAndSurroundingSpaces() {
        Map<String, SoundData> sounds = Map.of("s1", sound("s1", "pirate_bay"));

        assertThat(SoundService.isNameTaken(sounds, "  Pirate_Bay ", "s2")).isTrue();
        assertThat(SoundService.isNameTaken(sounds, "tavern", "s2")).isFalse();
    }

    @Test
    void isNameTaken_allowsTheSoundsOwnName() {
        Map<String, SoundData> sounds = Map.of("s1", sound("s1", "pirate_bay"));

        assertThat(SoundService.isNameTaken(sounds, "pirate_bay", "s1")).isFalse();
    }

    @Test
    void isNameTaken_withNullNameOrMap_isFalse() {
        assertThat(SoundService.isNameTaken(null, "x", "s1")).isFalse();
        assertThat(SoundService.isNameTaken(Map.of(), null, "s1")).isFalse();
    }
}
