package com.pdg.adventure.view.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;

import com.pdg.adventure.view.about.AboutView;
import com.pdg.adventure.view.adventure.AdventuresMenuView;

@StyleSheet(Lumo.STYLESHEET)  // loads the new lumo.css
// @Getter
public class AdventureAppLayout extends AppLayout implements AfterNavigationObserver {

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

    public void createDrawer(String anAppName) {
        drawer = createMyDrawer(anAppName, null);
        addToDrawer(drawer);
        getElement().executeJs(
            "this.__updateActiveDrawerItem = function() {" +
            "  const links = this.querySelectorAll('vaadin-side-nav a');" +
            "  const path = window.location.pathname;" +
            "  links.forEach(link => {" +
            "    link.parentElement.classList.toggle('vaadin-side-nav-item--selected'," +
            "      link.getAttribute('href') === path || " +
            "      (link.getAttribute('href') + '/').startsWith(path + '/') ||" +
            "      path.startsWith(link.getAttribute('href') + '/'));" +
            "  });" +
            "};" +
            "this.__updateActiveDrawerItem();" +
            "window.addEventListener('vaadin-router-location-changed', () => this.__updateActiveDrawerItem());"
        );
    }

    private VerticalLayout createMyDrawer(String anAppName, Image anAppImage) {

        H1 appName = new H1(anAppName);
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Header header = anAppImage == null
                        ? new Header(appName)
                        : new Header(anAppImage, appName);

        SideNav nav = new SideNav();

        nav.addItem(
                new SideNavItem("About", AboutView.class, VaadinIcon.INFO_CIRCLE.create()),
                new SideNavItem("Adventures", AdventuresMenuView.class, VaadinIcon.GRID.create())
        );

        // SideNavItem settings = new SideNavItem("Settings", VaadinIcon.COGS.create());
        // settings.addItem(new SideNavItem("Profile", ProfileView.class));
        // settings.addItem(new SideNavItem("Security", SecurityView.class));
        // nav.addItem(settings);

        VerticalLayout drawer = new VerticalLayout(header, nav);
        drawer.setSizeFull();
        drawer.setPadding(true);
        drawer.setSpacing(false);
        drawer.getThemeList().set("spacing-s", true);
        drawer.setAlignItems(FlexComponent.Alignment.STRETCH);

        return drawer;
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

    @Override
    public void afterNavigation(final AfterNavigationEvent aAfterNavigationEvent) {
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
