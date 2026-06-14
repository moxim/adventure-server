package com.pdg.adventure.view.admin;

import com.vaadin.flow.component.html.H2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.server.security.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserManagementViewTest {

    @Mock
    private UserService userService;

    @Test
    void heading_isUserFriendly_doesNotLeakInternalClassName() {
        when(userService.findAll()).thenReturn(List.of());

        UserManagementView view = new UserManagementView(userService);

        H2 heading = view.getChildren()
                .filter(H2.class::isInstance).map(H2.class::cast)
                .findFirst().orElseThrow();
        assertThat(heading.getText()).doesNotContain("UserData").contains("User");
    }
}
