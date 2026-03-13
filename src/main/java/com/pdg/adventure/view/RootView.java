package com.pdg.adventure.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.view.admin.AdminDahboardView;
import com.pdg.adventure.view.author.AuthorDashboardView;
import com.pdg.adventure.view.login.LoginView;
import com.pdg.adventure.view.player.PlayerLibraryView;

/**
 * Entry point for the application root ("/").
 * Vaadin's default post-login redirect targets this view; it dispatches to the
 * role-appropriate dashboard or to the login page for unauthenticated visitors.
 */
@Route(value = "")
@AnonymousAllowed
public class RootView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            event.rerouteTo(LoginView.class);
            return;
        }
        if (!(auth.getPrincipal() instanceof UserData user)) {
            event.rerouteTo(LoginView.class);
            return;
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            event.rerouteTo(AdminDahboardView.class);
        } else if (user.getRoles().contains(Role.AUTHOR)) {
            event.rerouteTo(AuthorDashboardView.class);
        } else {
            event.rerouteTo(PlayerLibraryView.class);
        }
    }
}
