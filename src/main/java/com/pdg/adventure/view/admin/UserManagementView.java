package com.pdg.adventure.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.UserService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "admin/users", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_ADMIN") // Only Admins can access this
public class UserManagementView extends VerticalLayout {

    private final transient UserService userService;
    private final Grid<UserData> grid = new Grid<>(UserData.class, false);

    public UserManagementView(UserService userService) {
        this.userService = userService;

        add(new H2("User Management"));

        configureGrid();
        updateList();

        Button addUserBtn = new Button("Add New User", e -> openUserForm(new UserData()));

        add(new HorizontalLayout(addUserBtn), ViewSupporter.doubleClickEditHint(), grid);
    }

    private void configureGrid() {
        grid.addColumn(UserData::getUsername).setHeader("Username");

        // Custom column to display roles nicely
        grid.addColumn(user -> user.getRoles().toString()).setHeader("Roles");

        grid.addColumn(UserData::isEnabled).setHeader("Enabled?");

        // Double-click a row to edit (consistent with every other grid: single-click selects, double-click opens).
        grid.addItemDoubleClickListener(event -> openUserForm(event.getItem()));
    }

    private void updateList() {
        grid.setItems(userService.findAll());
    }

    private void openUserForm(UserData user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(user.getId() == null ? "Create User" : "Edit User");

        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");

        // Show password field only for NEW users for simplicity.
        // Editing passwords usually requires a separate "Change Password" flow.
        if (user.getId() != null) {
            passwordField.setVisible(false); // Hide for editing
            usernameField.setValue(user.getUsername());
            usernameField.setReadOnly(true); // Usually don't allow changing username
        }

        // Dropdown for Roles
        MultiSelectComboBox<Role> rolesBox = new MultiSelectComboBox<>("Assign Roles");
        rolesBox.setItems(Role.values()); // ADMIN, AUTHOR, PLAYER
        rolesBox.setValue(user.getRoles()); // Pre-select current roles

        Checkbox enabledBox = new Checkbox("Account Active", user.isEnabled());

        Button saveBtn = new Button("Save", e -> {
            try {
                if (user.getId() == null) {
                    // Create New UserData Logic
                    String password = passwordField.getValue();
                    if (password.isEmpty()) {
                        Notification notification = Notification.show("Password cannot be empty", 5000, Notification.Position.MIDDLE);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                    userService.createUser(usernameField.getValue(), password, rolesBox.getValue());
                    Notification notification = Notification.show("User created successfully!", 2000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    // Update Existing UserData Logic
                    user.setRoles(rolesBox.getValue());
                    user.setEnabled(enabledBox.getValue());
                    userService.save(user);
                    Notification notification = Notification.show("User updated!", 2000, Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                updateList();
                dialog.close();
            } catch (Exception ex) {
                Notification notification = Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button deleteBtn = new Button("Delete", e -> {
            ConfirmDialog confirm = ViewSupporter.getConfirmDialog("Delete User", "user", user.getUsername());
            confirm.addConfirmListener(_ -> {
                userService.delete(user.getId());
                updateList();
                dialog.close();
                Notification notification = Notification.show("User deleted.", 2000, Notification.Position.BOTTOM_START);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            confirm.open();
        });

        deleteBtn.setVisible(user.getId() != null);
        if (user.getId() != null
                && user.getUsername().equals(ViewSupporter.getCurrentUser().getUsername())) {
            deleteBtn.setEnabled(false);
            deleteBtn.setTooltipText("You cannot delete your own account.");
        }

        dialog.add(new VerticalLayout(usernameField, passwordField, rolesBox, enabledBox, new HorizontalLayout(saveBtn, deleteBtn)));
        dialog.open();
    }
}
