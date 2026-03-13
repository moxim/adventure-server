package com.pdg.adventure.view.author;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("author/dashboard")
@RolesAllowed("ROLE_AUTHOR") // Only Authors (and Admins via hierarchy) can access
public class AuthorDashboardView extends VerticalLayout {
    public AuthorDashboardView() {
        Button gameManagementBtn = new Button("Game Management", e -> getUI().ifPresent(ui -> ui.navigate("author/adventures")));
        add(new H1("Author Dashboard - Create your stories here"), gameManagementBtn);
    }
}
