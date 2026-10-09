package com.pdg.adventure.view.component;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.util.Optional;

import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.view.about.AboutView;
import com.pdg.adventure.view.admin.AdminDashboardView;
import com.pdg.adventure.view.author.AuthorDashboardView;
import com.pdg.adventure.view.location.LocationMapView;
import com.pdg.adventure.view.login.LogoutView;
import com.pdg.adventure.view.player.PlayerLibraryView;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@StyleSheet(Lumo.STYLESHEET)
@PermitAll
public class AdventureAppLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {

    static final String APP_NAME = "Adventure Builder";
    private static final String COLOR_SCHEME_KEY = "adventure-color-scheme";

    private H2 viewTitle;
    private VerticalLayout drawer;
    private final Button colorSchemeToggle = new Button();
    private boolean dark;
    // The adventure's world map; links to the adventure named by the current route, hidden when there is none.
    private final SideNavItem worldItem = new SideNavItem("The World", "", VaadinIcon.GLOBE.create());

    public AdventureAppLayout() {
        worldItem.setVisible(false);
        createHeader(APP_NAME);
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

        viewTitle.getStyle().set("flex-grow", "1");
        HorizontalLayout header = new HorizontalLayout(toggle, img, viewTitle, createColorSchemeToggle());

        header.setId("header");
        header.getThemeList().set("dark", true);
        header.setSizeFull();
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        return header;
    }

    private Button createColorSchemeToggle() {
        colorSchemeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
        colorSchemeToggle.setId("color-scheme-toggle");
        colorSchemeToggle.addClickListener(e -> applyColorScheme(!dark, true));
        updateColorSchemeToggle();
        return colorSchemeToggle;
    }

    /**
     * Restores the author's saved choice; without one, the app keeps following the OS preference and the toggle
     * only reflects it.
     */
    @Override
    protected void onAttach(AttachEvent anAttachEvent) {
        super.onAttach(anAttachEvent);
        anAttachEvent.getUI().getPage().executeJs(
            "return localStorage.getItem($0) || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');",
            COLOR_SCHEME_KEY).then(String.class, saved -> applyColorScheme("dark".equals(saved), false));
    }

    private void applyColorScheme(boolean aDark, boolean aPersist) {
        dark = aDark;
        UI.getCurrent().getPage().setColorScheme(dark ? ColorScheme.Value.DARK : ColorScheme.Value.LIGHT);
        if (aPersist) {
            UI.getCurrent().getPage().executeJs("localStorage.setItem($0, $1)", COLOR_SCHEME_KEY,
                                                dark ? "dark" : "light");
        }
        updateColorSchemeToggle();
    }

    private void updateColorSchemeToggle() {
        colorSchemeToggle.setIcon((dark ? VaadinIcon.SUN_O : VaadinIcon.MOON_O).create());
        String label = dark ? "Switch to light mode" : "Switch to dark mode";
        colorSchemeToggle.setTooltipText(label);
        colorSchemeToggle.setAriaLabel(label);
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
        nav.addItem(new SideNavItem("About", AboutView.class, VaadinIcon.INFO_CIRCLE.create()));

        UserData user = ViewSupporter.getCurrentUser();
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Dashboard", AdminDashboardView.class, VaadinIcon.DASHBOARD.create()));
        } else if (user.isAuthor()) {
            nav.addItem( new SideNavItem("Dashboard", AuthorDashboardView.class, VaadinIcon.DASHBOARD.create()));
        } else if (user.isPlayer()) {
            nav.addItem(new SideNavItem("Library", PlayerLibraryView.class, VaadinIcon.BOOK.create()));
        }

        nav.addItem(worldItem);
        nav.addItem(new SideNavItem("Logout", LogoutView.class, VaadinIcon.SIGN_OUT.create()));

        // SideNavItem settings = new SideNavItem("Settings", VaadinIcon.COGS.create());
        // settings.addItem(new SideNavItem("Profile", ProfileView.class));
        // nav.addItem(settings);

        VerticalLayout myDrawer = new VerticalLayout(header, nav);
        myDrawer.setSizeFull();
        myDrawer.setPadding(true);
        myDrawer.setSpacing(false);
        myDrawer.getThemeList().set("spacing-s", true);
        myDrawer.setAlignItems(FlexComponent.Alignment.STRETCH);

        return myDrawer;
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

    /**
     * Offers the world map to authors as soon as the route names an adventure, whatever view they are in, their
     * test run included. Players never get it: it is an author's reference.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent aBeforeEnterEvent) {
        Optional<String> adventureId = aBeforeEnterEvent.getRouteParameters().get(RouteIds.ADVENTURE_ID.getValue());
        boolean offered = adventureId.isPresent() && ViewSupporter.getCurrentUser().isAuthor();
        if (offered) {
            worldItem.setPath(LocationMapView.class,
                              new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                 adventureId.get())));
        }
        worldItem.setVisible(offered);
    }

    @Override
    public void afterNavigation(final AfterNavigationEvent aAfterNavigationEvent) {
        viewTitle.setText(navbarTitleOrDefault(getCurrentPageTitle()));
    }

    /** The navbar title for the current view, falling back to the app name when a view declares none. */
    static String navbarTitleOrDefault(String pageTitle) {
        return (pageTitle == null || pageTitle.isBlank()) ? APP_NAME : pageTitle;
    }

    private String getCurrentPageTitle() {
        if (getContent() instanceof HasDynamicTitle dyna) {
            return dyna.getPageTitle();
        }
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
