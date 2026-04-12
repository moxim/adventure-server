package com.pdg.adventure.server.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;

@Repository
public interface UserRepository extends JpaRepository<UserData, String> {
    // Spring automatically implements this query based on the method name
    Optional<UserData> findByUsername(String username);

    List<UserData> findByRolesContaining(Role role);
}
