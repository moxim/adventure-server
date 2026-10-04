package com.pdg.adventure.server.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.AdventureAuthor;
import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.repository.AdventureAuthorRepository;
import com.pdg.adventure.server.security.repository.AdventurePlayerRepository;
import com.pdg.adventure.server.storage.service.AdventureDuplicator;
import com.pdg.adventure.server.storage.service.AdventureService;

class AdventureAccessServiceDuplicateTest {

    private AdventureService adventureService;
    private AdventureDuplicator adventureDuplicator;
    private AdventureAuthorRepository authorRepository;
    private AdventureAccessService accessService;

    private UserData author;
    private AdventureData source;
    private AdventureData copy;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        adventureDuplicator = mock(AdventureDuplicator.class);
        authorRepository = mock(AdventureAuthorRepository.class);
        accessService = new AdventureAccessService(adventureService, adventureDuplicator, authorRepository,
                                                   mock(AdventurePlayerRepository.class));

        author = user("anna-id", Role.AUTHOR);

        source = new AdventureData();
        source.setId("src");
        source.setTitle("The Demo");
        copy = new AdventureData();
        copy.setId("dup");
        copy.setTitle("The Demo (copy)");

        when(adventureService.findAdventureById("src")).thenReturn(Optional.of(source));
        when(adventureService.findAdventureById("dup")).thenReturn(Optional.of(copy));
        when(adventureDuplicator.duplicate("src", "The Demo (copy)")).thenReturn("dup");
    }

    private void authorOwnsSource() {
        when(authorRepository.findByAdventureId("src")).thenReturn(Optional.of(new AdventureAuthor("src", author)));
    }

    @Test
    void duplicate_copiesTheAdventureAndMakesTheCurrentUserItsAuthor() {
        authorOwnsSource();

        AdventureData result = accessService.duplicateAdventure("src", author);

        assertThat(result).isSameAs(copy);
        ArgumentCaptor<AdventureAuthor> saved = ArgumentCaptor.forClass(AdventureAuthor.class);
        verify(authorRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getAdventureId()).isEqualTo("dup");
        assertThat(saved.getValue().getUser()).isSameAs(author);
    }

    @Test
    void duplicate_isRefusedForAnAuthorWhoDoesNotOwnTheAdventure() {
        UserData owner = otherUser();
        when(authorRepository.findByAdventureId("src")).thenReturn(Optional.of(new AdventureAuthor("src", owner)));

        assertThatThrownBy(() -> accessService.duplicateAdventure("src", author))
                .isInstanceOf(AccessDeniedException.class);
        verify(adventureDuplicator, never()).duplicate(anyString(), anyString());
        verify(authorRepository, never()).saveAndFlush(any());
    }

    @Test
    void duplicate_isAllowedForAnAdmin() {
        UserData admin = user("admin-id", Role.ADMIN);

        AdventureData result = accessService.duplicateAdventure("src", admin);

        assertThat(result).isSameAs(copy);
    }

    @Test
    void duplicate_removesTheCopyAgainWhenTheAuthorAssignmentFails() {
        authorOwnsSource();
        doThrow(new IllegalStateException("mysql down")).when(authorRepository).saveAndFlush(any());

        assertThatThrownBy(() -> accessService.duplicateAdventure("src", author))
                .isInstanceOf(IllegalStateException.class);

        verify(adventureService).deleteAdventure("dup");
    }

    private UserData otherUser() {
        return user("bob-id", Role.AUTHOR);
    }

    // UserData has no id setter (ids are generated on persist), so stub the two getters the access checks read
    private static UserData user(String id, Role role) {
        UserData user = mock(UserData.class);
        when(user.getId()).thenReturn(id);
        when(user.getRoles()).thenReturn(Set.of(role));
        return user;
    }
}
