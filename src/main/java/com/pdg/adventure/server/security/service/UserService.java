package com.pdg.adventure.server.security.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserData> findAll() {
        return userRepository.findAll();
    }

    // Used when creating a brand new user
    public UserData createUser(String username, String rawPassword, Set<Role> roles) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }

        UserData user = new UserData();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword)); // ENCRYPT!
        user.setRoles(roles);
        user.setEnabled(true); // Activate by default

        return userRepository.save(user);
    }

    // Used for updates (like changing roles or disabling accounts)
    public UserData save(UserData user) {
        return userRepository.save(user);
    }

    public List<UserData> findByRole(Role role) {
        return userRepository.findByRolesContaining(role);
    }

    public void delete(String id) {
        userRepository.deleteById(id);
    }
}
