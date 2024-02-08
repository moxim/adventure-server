package com.pdg.adventure.views.components;

import com.pdg.adventure.views.about.AboutView;
import com.pdg.adventure.views.adventure.AdventuresMenuView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.Getter;

@Getter
public class AdventureAppLayout extends AppLayout {

    private H2 viewTitle;
    private VerticalLayout drawer;

    public void createHeader(String aTitle) {
        final HorizontalLayout header = createMyHeader(aTitle);
        addToNavbar(header);
    }

    public void createDrawer(String anAppName) {
        drawer = createMyDrawer(anAppName);
        addToDrawer(drawer);
    }

    public void extendDrawer(Component... components) {
        for (Component component : components) {
            drawer.add(component);
        }
    }

    private HorizontalLayout createMyHeader(String aTitle) {
        viewTitle = new H2(aTitle);
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        Image img = new Image("images/adventure.png", aTitle);
        img.setWidth("30px");

        HorizontalLayout header = new HorizontalLayout(toggle, img, viewTitle);

        header.setId("header");
        header.getThemeList().set("dark", true);
        header.setWidthFull();
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        return header;
    }

    private VerticalLayout createMyDrawer(String anAppName) {

        H1 appName = new H1(anAppName);
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        RouterLink aboutLink = new RouterLink("About", AboutView.class);
        aboutLink.setHighlightCondition(HighlightConditions.sameLocation());
        RouterLink adventureLink = new RouterLink("Adventures", AdventuresMenuView.class);
        adventureLink.setHighlightCondition(HighlightConditions.sameLocation());

        final VerticalLayout drawer = new VerticalLayout(header, aboutLink, adventureLink);

        drawer.setSizeFull();
        drawer.setPadding(true);
        drawer.setSpacing(false);
        drawer.getThemeList().set("spacing-s", true);
        drawer.setAlignItems(FlexComponent.Alignment.STRETCH);

        return drawer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getViewTitle().setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        if (getContent() instanceof HasDynamicTitle dyna) {
            return dyna.getPageTitle();
        }
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
