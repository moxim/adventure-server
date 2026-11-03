package com.pdg.adventure.view.support;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;

/**
 * Keeps the browser's tab history from filling up with every view an author passes through.
 * <p>
 * Two pieces cooperate:
 * <ul>
 *   <li>{@link #TRAIL_SCRIPT} runs once per page load (see {@link HistoryNavigationInitializer}) and remembers, per
 *       history position, which path was shown there. That is the only way to know what the browser's previous
 *       history entry is - the server never sees it.</li>
 *   <li>{@link #navigateUp} is what a Back button calls. If the previous history entry is the view we are about to
 *       "navigate" to anyway, it steps back in the history instead of pushing the same view a second time.</li>
 * </ul>
 * Anything the browser history cannot answer for sure (refresh, deep link, a different previous view) falls back to
 * the plain {@link UI#navigate} that every Back button used before, so behaviour only ever differs by not adding a
 * redundant entry.
 */
public final class HistoryNavigation {

    /**
     * Records {@code history position -> path} on every completed navigation and offers
     * {@code window.adventureTrail.stepBackTo(path)}. Positions come from the client router's {@code history.state.idx};
     * the record lives in {@code sessionStorage} so it survives a page refresh, and is per tab.
     * Paths are compared without leading or trailing slash, relative to the application's base URI.
     */
    static final String TRAIL_SCRIPT = """
            (() => {
              if (window.adventureTrail) { return; }
              const KEY = 'adventure-nav-trail';
              const base = new URL(document.baseURI).pathname;
              const norm = p => (p.startsWith(base) ? p.slice(base.length) : p).replace(/^\\/+|\\/+$/g, '');
              const load = () => { try { return JSON.parse(sessionStorage.getItem(KEY) || '{}'); } catch (e) { return {}; } };
              const position = () => (history.state && typeof history.state.idx === 'number') ? history.state.idx : null;
              const record = () => {
                const idx = position();
                if (idx === null) { return; }
                const trail = load();
                trail[idx] = norm(location.pathname);
                try { sessionStorage.setItem(KEY, JSON.stringify(trail)); } catch (e) { /* storage unavailable */ }
              };
              window.adventureTrail = {
                // true (and one step back done) only if the previous history entry is known to be `parent`
                stepBackTo: parent => {
                  const idx = position();
                  if (idx === null || idx < 1 || load()[idx - 1] !== norm(parent)) { return false; }
                  history.back();
                  return true;
                }
              };
              window.addEventListener('vaadin-navigated', record);
              record();
            })();
            """;

    /**
     * Makes the drawer's links replace the current history entry instead of adding one, so switching between
     * sections does not turn the browser's Back button into a trail of everything the author visited.
     * Runs against the {@code vaadin-side-nav}; its {@code onNavigate} hook cancels the link's own (pushing)
     * navigation, and modifier clicks (new tab/window) keep their default behaviour.
     */
    // TODO: Review needed — drawer switches now replace instead of push, so after switching section through the
    //  drawer the browser Back button no longer returns to the view that was left. Confirm this is the wanted behaviour.
    public static final String DRAWER_REPLACE_SCRIPT = """
            this.onNavigate = ({ path }) => {
              window.dispatchEvent(new CustomEvent('vaadin-navigate', {
                detail: { state: null, url: path.replace(/^\\/+/, ''), replace: true, callback: true }
              }));
            };
            """;

    // $0 = path of the parent view. Resolves to whether the browser stepped back (see TRAIL_SCRIPT).
    private static final String STEP_BACK_SCRIPT =
            "return !!(window.adventureTrail && window.adventureTrail.stepBackTo($0));";

    private HistoryNavigation() {
    }

    /** Starts the history recording in the browser; call once per UI. */
    static void installTrail(UI aUi) {
        aUi.getPage().executeJs(TRAIL_SCRIPT);
    }

    /** {@link #navigateUp(Class, RouteParameters, boolean)} for a view with no route parameters and nothing to save. */
    public static void navigateUp(Class<? extends Component> aParent) {
        navigateUp(aParent, RouteParameters.empty(), false);
    }

    /** {@link #navigateUp(Class, RouteParameters, boolean)} for a view with no route parameters. */
    public static void navigateUp(Class<? extends Component> aParent, boolean aUnsavedChanges) {
        navigateUp(aParent, RouteParameters.empty(), aUnsavedChanges);
    }

    /** {@link #navigateUp(Class, RouteParameters, boolean)} for a view with nothing to save. */
    public static void navigateUp(Class<? extends Component> aParent, RouteParameters aParameters) {
        navigateUp(aParent, aParameters, false);
    }

    /**
     * Back to the given parent view.
     * <p>
     * With unsaved changes this is always a plain navigation: the "uncommitted changes" prompt then works exactly
     * as it does for every other way of leaving, including asking again after Cancel. (A browser history step is
     * answered only once by that prompt: after the first Cancel, further history steps are ignored.)
     *
     * @param aParent          the view to go back to
     * @param aParameters      its route parameters
     * @param aUnsavedChanges  whether the current view holds changes its leave-guard would ask about
     */
    public static void navigateUp(Class<? extends Component> aParent, RouteParameters aParameters,
                                  boolean aUnsavedChanges) {
        final UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        if (aUnsavedChanges) {
            ui.navigate(aParent, aParameters);
            return;
        }
        final String parentPath;
        try {
            parentPath = RouteConfiguration.forRegistry(ui.getInternals().getRouter().getRegistry())
                                           .getUrl(aParent, aParameters);
        } catch (RuntimeException _) {
            ui.navigate(aParent, aParameters);
            return;
        }
        ui.getPage().executeJs(STEP_BACK_SCRIPT, parentPath).then(Boolean.class, steppedBack -> {
            if (!Boolean.TRUE.equals(steppedBack)) {
                ui.navigate(aParent, aParameters);
            }
        }, _ -> ui.navigate(aParent, aParameters));
    }
}
