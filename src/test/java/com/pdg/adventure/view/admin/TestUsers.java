package com.pdg.adventure.view.admin;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;

/**
 * Builds UserData fixtures for admin-view tests. UserData has no setId
 * (JPA @PrePersist generates it), so the id is set via reflection to make
 * fixtures read as EXISTING users.
 */
final class TestUsers {

    private TestUsers() {
    }

    static UserData user(String anId, String aName, Role... someRoles) {
        try {
            UserData userData = new UserData();
            Field idField = UserData.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userData, anId);
            userData.setUsername(aName);
            userData.setPassword("irrelevant");
            userData.setRoles(new HashSet<>(Set.of(someRoles)));
            userData.setEnabled(true);
            return userData;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not build UserData fixture", e);
        }
    }
}
