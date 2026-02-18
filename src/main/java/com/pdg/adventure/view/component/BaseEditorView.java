package com.pdg.adventure.view.component;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.support.RouteIds;

public abstract class BaseEditorView<T> extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver,
        BeforeLeaveObserver {
    protected final transient AdventureService adventureService;
    protected final Binder<T> binder;
    protected final ResetBackSaveView navigationButtons;
    protected String pageTitle;
    protected AdventureData adventureData;
    protected String adventureId;
    protected String locationId;

    public BaseEditorView(AdventureService service, Class<T> beanType) {
        this.adventureService = service;
        this.binder = new BeanValidationBinder<>(beanType);
        this.navigationButtons = new ResetBackSaveView();
        configureNavigationButtons();
    }

    protected void configureNavigationButtons() {
        navigationButtons.getBack().addClickListener(_ -> navigateBack());
        navigationButtons.getSave().addClickListener(_ -> save());
        navigationButtons.getReset().addClickListener(_ -> binder.readBean(binder.getBean()));
        navigationButtons.getCancel().addClickShortcut(Key.ESCAPE);
        binder.addStatusChangeListener(e -> {
            navigationButtons.getSave().setEnabled(e.getBinder().hasChanges() && e.getBinder().isValid());
            navigationButtons.getReset().setEnabled(e.getBinder().hasChanges());
        });
    }

    protected abstract void navigateBack();

    protected abstract void save();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        adventureId = event.getRouteParameters().get(RouteIds.ADVENTURE_ID.getValue()).orElse("new");
        locationId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue()).orElse("new");
        pageTitle = getDefaultPageTitle();
    }

    protected abstract String getDefaultPageTitle();

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }
}
