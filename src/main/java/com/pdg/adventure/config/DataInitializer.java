package com.pdg.adventure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Check if admin exists. If not, create one.
        if (userRepository.findByUsername("admin").isEmpty()) {
            UserData admin = new UserData();
            admin.setUsername("admin");
            String adminPassword = UUID.randomUUID().toString();
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(Role.ADMIN, Role.AUTHOR, Role.PLAYER));
            admin.setEnabled(true);

            userRepository.save(admin);
            LOG.info(">>> Default Admin user created: admin / {}", adminPassword);
        } else {
            LOG.info(">>> Admin user already exists. Skipping creation.");
        }
    }
}
