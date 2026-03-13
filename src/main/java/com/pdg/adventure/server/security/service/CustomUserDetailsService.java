package com.pdg.adventure.server.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserData user = userRepository.findByUsername(username)
                                      .orElseThrow(() -> new UsernameNotFoundException("UserData not found with username: " + username));
         LOG.info("Loading UserDetails for {}, roles: {}", username,  user.getRoles());
        return user;
    }
}
