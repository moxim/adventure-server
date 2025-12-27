package com.pdg.adventure.view.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.StyleSheet;
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
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;

import com.pdg.adventure.view.about.AboutView;
import com.pdg.adventure.view.adventure.AdventuresMenuView;

@StyleSheet(Lumo.STYLESHEET)  // loads the new lumo.css
// @Getter
public class AdventureAppLayout extends AppLayout {

    private H2 viewTitle;
    private VerticalLayout drawer;

    public AdventureAppLayout() {
        String title = "Adventure Builder";
        createHeader(title);
    }

    public void createHeader(String aTitle) {
        final HorizontalLayout header = createMyHeader(aTitle);
        addToNavbar(header);
    }

    public void createDrawer(String anAppName) {
        drawer = createMyDrawer(anAppName, null);
        addToDrawer(drawer);
    }

    public void createDrawer(String anAppName, Image anImage) {
        drawer = createMyDrawer(anAppName, anImage);
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
        header.setSizeFull();
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        return header;
    }

    private VerticalLayout createMyDrawer(String anAppName, Image anAppImage) {

        H1 appName = new H1(anAppName);
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        HorizontalLayout headerRow = new HorizontalLayout(appName);
        Header header = anAppImage == null ? new Header(appName) : new Header(anAppImage, appName);
        headerRow.add(header);

        RouterLink aboutLink = new RouterLink("About", AboutView.class);
        aboutLink.setHighlightCondition(HighlightConditions.sameLocation());
        RouterLink adventureLink = new RouterLink("Adventures", AdventuresMenuView.class);
        adventureLink.setHighlightCondition(HighlightConditions.sameLocation());

        final VerticalLayout drawer = new VerticalLayout(headerRow, aboutLink, adventureLink);

        drawer.setSizeFull();
        drawer.setPadding(true);
        drawer.setSpacing(false);
        drawer.getThemeList().set("spacing-s", true);
        drawer.setAlignItems(FlexComponent.Alignment.STRETCH);

        return drawer;
    }

    protected void afterNavigation() { // TODO: remove if not needed
        if (isOverlay()) {
            setDrawerOpened(false);
        }
        viewTitle.setText(getCurrentPageTitle());
    }


    private String getCurrentPageTitle() {
        if (getContent() instanceof HasDynamicTitle dyna) {
            return dyna.getPageTitle();
        }
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
