package com.pdg.adventure.server.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        RoleHierarchy roleHierarchy = RoleHierarchyImpl.fromHierarchy("""
                ROLE_ADMIN > ROLE_AUTHOR
                ROLE_AUTHOR > ROLE_PLAYER
                """);
        userDetailsService = new CustomUserDetailsService(userRepository, roleHierarchy);
    }

    @Test
    void loadUserByUsername_shouldExpandAuthorities_whenUserIsAdmin() {
        UserData admin = userWithRoles("admin", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(authorityNames(result)).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_AUTHOR", "ROLE_PLAYER");
    }

    @Test
    void loadUserByUsername_shouldExpandAuthorities_whenUserIsAuthor() {
        UserData author = userWithRoles("author", Role.AUTHOR);
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));

        UserDetails result = userDetailsService.loadUserByUsername("author");

        assertThat(authorityNames(result)).containsExactlyInAnyOrder("ROLE_AUTHOR", "ROLE_PLAYER");
    }

    @Test
    void loadUserByUsername_shouldNotAddAuthorities_whenUserIsPlayer() {
        UserData player = userWithRoles("player", Role.PLAYER);
        when(userRepository.findByUsername("player")).thenReturn(Optional.of(player));

        UserDetails result = userDetailsService.loadUserByUsername("player");

        assertThat(authorityNames(result)).containsExactlyInAnyOrder("ROLE_PLAYER");
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private static UserData userWithRoles(String username, Role... roles) {
        UserData user = new UserData();
        user.setUsername(username);
        user.setPassword("irrelevant");
        user.setRoles(Set.of(roles));
        return user;
    }

    private static Set<String> authorityNames(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
