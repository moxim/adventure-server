package com.pdg.adventure.view.component;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.view.adventure.AdventureRunView;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.command.CommandMainLayout;
import com.pdg.adventure.view.direction.DirectionsMainLayout;
import com.pdg.adventure.view.item.ItemsMainLayout;
import com.pdg.adventure.view.location.LocationsMainLayout;
import com.pdg.adventure.view.message.MessagesMainLayout;
import com.pdg.adventure.view.picture.PicturesMainLayout;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.vocabulary.VocabularyMainLayout;
import com.pdg.adventure.view.workflow.WorkflowMainLayout;

/** Every layout of an adventure's editors offers "The World" as soon as the route names the adventure. */
class AdventureAppLayoutWorldEntryTest extends BrowserlessTest {

    static Stream<Supplier<AdventureAppLayout>> layouts() {
        return Stream.of(AdventuresMainLayout::new, LocationsMainLayout::new, MessagesMainLayout::new,
                         ItemsMainLayout::new, PicturesMainLayout::new, VocabularyMainLayout::new,
                         WorkflowMainLayout::new, CommandMainLayout::new, DirectionsMainLayout::new);
    }

    @BeforeEach
    void setUp() {
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of(Role.AUTHOR));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdventureAppLayout attach(Supplier<AdventureAppLayout> aFactory) {
        AdventureAppLayout layout = aFactory.get();
        UI.getCurrent().add(layout);
        return layout;
    }

    /** Queries only see visible components, so a hidden entry is simply not found. */
    private List<SideNavItem> shownWorldEntries(AdventureAppLayout aLayout) {
        return find(SideNavItem.class, aLayout).withText("The World").all();
    }

    private static void enter(AdventureAppLayout aLayout, RouteParameters someParameters) {
        enter(aLayout, someParameters, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void enter(AdventureAppLayout aLayout, RouteParameters someParameters, Class aTarget) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(someParameters);
        when(event.getNavigationTarget()).thenReturn(aTarget);
        aLayout.beforeEnter(event);
    }

    private static void loginAs(Role aRole) {
        UserData user = new UserData();
        user.setUsername("test-user");
        user.setRoles(Set.of(aRole));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private static RouteParameters adventure(String anId) {
        return new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), anId));
    }

    @ParameterizedTest
    @MethodSource("layouts")
    void theWorldEntryWaitsForAnAdventure(Supplier<AdventureAppLayout> aFactory) {
        assertThat(shownWorldEntries(attach(aFactory))).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("layouts")
    void theWorldEntryLinksToTheMapOfTheAdventureBeingEdited(Supplier<AdventureAppLayout> aFactory) {
        AdventureAppLayout layout = attach(aFactory);

        enter(layout, adventure("adv-1"));

        assertThat(shownWorldEntries(layout)).singleElement()
                .extracting(SideNavItem::getPath).isEqualTo("author/adventures/adv-1/map");
    }

    @Test
    void theWorldEntryFollowsTheAdventureWhenItChanges() {
        AdventureAppLayout layout = attach(LocationsMainLayout::new);

        enter(layout, adventure("adv-1"));
        enter(layout, adventure("adv-2"));

        assertThat(shownWorldEntries(layout)).singleElement()
                .extracting(SideNavItem::getPath).isEqualTo("author/adventures/adv-2/map");
    }

    @Test
    void theWorldEntryIsHiddenAgainWithoutAnAdventure() {
        AdventureAppLayout layout = attach(LocationsMainLayout::new);

        enter(layout, adventure("adv-1"));
        enter(layout, new RouteParameters());

        assertThat(shownWorldEntries(layout)).isEmpty();
    }

    @Test
    void theAuthorKeepsTheWorldEntryDuringTheTestRun() {
        AdventureAppLayout layout = attach(AdventuresMainLayout::new);

        enter(layout, adventure("adv-1"), AdventureRunView.class);

        assertThat(shownWorldEntries(layout)).singleElement()
                .extracting(SideNavItem::getPath).isEqualTo("author/adventures/adv-1/map");
    }

    @Test
    void thePlayerDoesNotSeeTheWorldEntryWhilePlaying() {
        loginAs(Role.PLAYER);
        AdventureAppLayout layout = attach(AdventuresMainLayout::new);

        enter(layout, adventure("adv-1"), AdventureRunView.class);

        assertThat(shownWorldEntries(layout)).isEmpty();
    }

    @Test
    void thePlayerNeverSeesTheWorldEntry() {
        loginAs(Role.PLAYER);
        AdventureAppLayout layout = attach(AdventuresMainLayout::new);

        enter(layout, adventure("adv-1"));

        assertThat(shownWorldEntries(layout)).isEmpty();
    }
}
