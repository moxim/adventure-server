package com.pdg.adventure.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import com.pdg.adventure.view.adventure.AdventuresMainLayout;

@Route(value = "admin/dashboard", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminDashboardView extends VerticalLayout {

    public AdminDashboardView() {
        Button userManagementBtn = new Button("User Management", e -> getUI().ifPresent(ui -> ui.navigate("admin/users")));
        Button gameManagementBtn = new Button("Game Management", e -> getUI().ifPresent(ui -> ui.navigate("author/adventures")));
        Button assignmentsBtn = new Button("Adventure Assignments", e -> getUI().ifPresent(ui -> ui.navigate("admin/adventures/assignments")));
        Button statsBtn = new Button("View Statistics", e -> getUI().ifPresent(ui -> ui.navigate("admin/stats")));

        add(userManagementBtn, gameManagementBtn, assignmentsBtn, statsBtn);
    }
}
