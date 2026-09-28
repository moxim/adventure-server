package com.pdg.adventure.server.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    private final RoleHierarchy roleHierarchy;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, RoleHierarchy roleHierarchy) {
        this.userRepository = userRepository;
        this.roleHierarchy = roleHierarchy;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserData user = userRepository.findByUsername(username)
                                      .orElseThrow(() -> new UsernameNotFoundException("UserData not found with username: " + username));
        // Expand to every authority reachable via the ADMIN > AUTHOR > PLAYER hierarchy so both
        // Spring's authorizeHttpRequests matchers and Vaadin's @RolesAllowed/isUserInRole checks
        // (which never consult RoleHierarchy on their own) see the full set.
        user.setAuthorities(roleHierarchy.getReachableGrantedAuthorities(user.getAuthorities()));
        LOG.info("Loading UserDetails for {}, roles: {}", username, user.getRoles());
        return user;
    }
}
