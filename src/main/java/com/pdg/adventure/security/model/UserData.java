package com.pdg.adventure.security.model;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserData implements UserDetails {

    @Id
    @Column(name = "id", length = 26, nullable = false, updatable = false)
    @Getter
    private String id;

    @PrePersist
    void generateId() {
        if (id == null) {
            id = UlidCreator.getUlid().toString();
        }
    }

    @Column(unique = true, nullable = false)
    @Getter
    @Setter
    private String username;

    @Column(nullable = false)
    @Getter
    @Setter
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private Set<Role> roles;

    @Getter
    @Setter
    private boolean isEnabled;

    // Role-hierarchy-expanded authorities for the current session, populated by
    // CustomUserDetailsService. Kept transient (never persisted) so that expanding
    // an ADMIN's authorities to include AUTHOR/PLAYER can never be dirty-checked
    // back into the persisted `roles` collection.
    @Transient
    private transient Collection<? extends GrantedAuthority> effectiveAuthorities;

    // Getters and Setters...

    // UserDetails implementation methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Objects.requireNonNullElseGet(effectiveAuthorities, () -> roles.stream()
                                                                              .map(role -> new SimpleGrantedAuthority(
                                                                                      "ROLE_" + role.name()))
                                                                              .toList());
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.effectiveAuthorities = authorities;
    }

    public boolean isAdmin() {
        return roles.contains(Role.ADMIN);
    }

    public boolean isAuthor() {
        return roles.contains(Role.AUTHOR);
    }

    public boolean isPlayer() {
        return roles.contains(Role.PLAYER);
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
