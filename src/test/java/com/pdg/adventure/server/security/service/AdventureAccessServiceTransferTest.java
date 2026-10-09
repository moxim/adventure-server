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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.AdventureAuthor;
import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.exception.AdventureImportException;
import com.pdg.adventure.server.security.repository.AdventureAuthorRepository;
import com.pdg.adventure.server.security.repository.AdventurePlayerRepository;
import com.pdg.adventure.server.storage.service.AdventureDuplicator;
import com.pdg.adventure.server.storage.service.AdventureExporter;
import com.pdg.adventure.server.storage.service.AdventureImporter;
import com.pdg.adventure.server.storage.service.AdventureService;

class AdventureAccessServiceTransferTest {

    private static final byte[] FILE = {1, 2, 3};

    private AdventureService adventureService;
    private AdventureExporter exporter;
    private AdventureImporter importer;
    private AdventureAuthorRepository authorRepository;
    private AdventureAccessService accessService;

    private UserData author;
    private AdventureData imported;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        exporter = mock(AdventureExporter.class);
        importer = mock(AdventureImporter.class);
        authorRepository = mock(AdventureAuthorRepository.class);
        accessService = new AdventureAccessService(adventureService, mock(AdventureDuplicator.class), exporter,
                                                   importer, authorRepository, mock(AdventurePlayerRepository.class));

        author = user("anna-id", Role.AUTHOR);
        imported = new AdventureData();
        imported.setId("new-id");
        when(adventureService.findAdventureById("new-id")).thenReturn(Optional.of(imported));
        when(importer.importAdventure(FILE)).thenReturn(new AdventureImporter.ImportResult("new-id", true));
        when(exporter.export("src")).thenReturn(FILE);
    }

    private void authorOwnsSource() {
        when(authorRepository.findByAdventureId("src")).thenReturn(Optional.of(new AdventureAuthor("src", author)));
    }

    @Test
    void export_returnsTheFileForTheOwningAuthor() {
        authorOwnsSource();

        assertThat(accessService.exportAdventure("src", author)).isEqualTo(FILE);
    }

    @Test
    void export_isAllowedForAnAdmin() {
        assertThat(accessService.exportAdventure("src", user("admin-id", Role.ADMIN))).isEqualTo(FILE);
    }

    @Test
    void export_isRefusedForAnAuthorWhoDoesNotOwnTheAdventure() {
        UserData owner = user("bob-id", Role.AUTHOR);
        when(authorRepository.findByAdventureId("src")).thenReturn(Optional.of(new AdventureAuthor("src", owner)));

        assertThatThrownBy(() -> accessService.exportAdventure("src", author))
                .isInstanceOf(AccessDeniedException.class);
        verify(exporter, never()).export(any());
    }

    @Test
    void import_makesTheImportingUserTheAuthorAndReportsAVersionMismatch() {
        AdventureAccessService.ImportedAdventure result = accessService.importAdventure(FILE, author);

        ArgumentCaptor<AdventureAuthor> saved = ArgumentCaptor.forClass(AdventureAuthor.class);
        verify(authorRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getAdventureId()).isEqualTo("new-id");
        assertThat(saved.getValue().getUser()).isSameAs(author);
        assertThat(result.adventure()).isSameAs(imported);
        assertThat(result.builderVersionDiffers()).isTrue();
    }

    @Test
    void import_isAllowedForAnAdminWithoutTheAuthorRole() {
        AdventureAccessService.ImportedAdventure result =
                accessService.importAdventure(FILE, user("admin-id", Role.ADMIN));

        assertThat(result.adventure()).isSameAs(imported);
    }

    @Test
    void import_isRefusedForAPlayer() {
        assertThatThrownBy(() -> accessService.importAdventure(FILE, user("pat-id", Role.PLAYER)))
                .isInstanceOf(AccessDeniedException.class);
        verify(importer, never()).importAdventure(any());
    }

    @Test
    void import_savesNoAuthorRowWhenTheFileIsRejected() {
        when(importer.importAdventure(FILE)).thenThrow(new AdventureImportException("This is not an adventure file."));

        assertThatThrownBy(() -> accessService.importAdventure(FILE, author))
                .isInstanceOf(AdventureImportException.class);
        verify(authorRepository, never()).saveAndFlush(any());
    }

    @Test
    void import_removesTheImportedDocumentsAgainWhenTheAuthorAssignmentFails() {
        doThrow(new IllegalStateException("mysql down")).when(authorRepository).saveAndFlush(any());

        assertThatThrownBy(() -> accessService.importAdventure(FILE, author))
                .isInstanceOf(IllegalStateException.class);

        verify(adventureService).deleteAdventure("new-id");
    }

    @Test
    void getMaxImportBytes_asksTheImporter() {
        when(importer.getMaxBytes()).thenReturn(42L);

        assertThat(accessService.getMaxImportBytes()).isEqualTo(42L);
    }

    // UserData has no id setter (ids are generated on persist), so stub the two getters the access checks read
    private static UserData user(String id, Role role) {
        UserData user = mock(UserData.class);
        when(user.getId()).thenReturn(id);
        when(user.getRoles()).thenReturn(Set.of(role));
        return user;
    }
}
