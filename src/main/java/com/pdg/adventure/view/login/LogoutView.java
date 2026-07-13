package com.pdg.adventure.view.login;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@Route("logout")
@PermitAll
public class LogoutView extends Main {

    public LogoutView(AuthenticationContext authenticationContext) {
        // Log out immediately on navigation: Spring Security invalidates the session
        // and redirects to the login page, so the user doesn't have to click again.
        authenticationContext.logout();
    }
}
