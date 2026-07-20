# Custom Route-Not-Found Page (UX Audit P1.3) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Unknown URLs show a friendly, on-brand "not found" page (using the new puzzled-dragon image) with a real 404 status, instead of an empty 403 that Chrome misinterprets as a file download.

**Architecture:** Two independent Vaadin/Spring Security defaults compound today: `VaadinSecurityConfigurer`'s catch-all rule is `denyAll()` (blocks unmatched requests at the HTTP layer before Vaadin's router ever runs), and Vaadin's built-in `RouteNotFoundError` view carries no access annotation (so even if a request reaches the router, navigation access control denies rendering it). The fix relaxes the catch-all to `permitAll()` and replaces the default view with a custom, `@AnonymousAllowed`, standalone (no parent layout) view. Full rationale and doc citations: `docs/superpowers/specs/2026-07-08-route-not-found-page-design.md`.

**Tech Stack:** Java 21+, Vaadin Flow 25.2.1, Spring Security 6/7 (`spring-security-config` 7.1.0) via `VaadinSecurityConfigurer`, JUnit 5 + Mockito + AssertJ (plain unit tests, no Spring/browserless context needed for the new view).

## Global Constraints

- Git repository root is `server/` — run ALL `git` and `mvn` commands from `/Users/mafw/workroom/projects/adventurebuilder/server`.
- Work on a feature branch off `actions-we-want-actions` (create via superpowers:using-git-worktrees at execution time), e.g. `route-not-found-page`.
- Commit messages follow Conventional Commits (`feat:`, `docs:`) and end with the `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>` trailer.
- New view class name: `RouteNotFoundView`, package `com.pdg.adventure.view.error` (new package). Matches this project's `*View` naming convention (`AGENTS-views.md`: "Standard view | `*View` | General-purpose screen (e.g., `RootView`, `AdminDashboardView`)").
- The view **must** be annotated `@AnonymousAllowed` and **must not** carry a `@ParentLayout`. `AdventuresMainLayout` (used by `AboutView` and most other views) is itself `@PermitAll`; nesting this view inside it would make Vaadin's navigation access control re-block anonymous visitors, defeating the fix. This is a hard constraint, not a style choice — do not "improve" it later by adding a parent layout without re-reading the design doc's rationale.
- Exact copy (verbatim, approved): heading `"Even the dragon is puzzled."`; body `"There's no path here — '{attempted path}' doesn't lead anywhere in this world."`; button `"Back to safety"`.
- Image: `images/main_puzzled.jpg`, alt text `"A giant puzzled dragon towers over a small armored knight"`, width `300px` (matches `AboutView`'s treatment of `images/main.jpg`).
- Verified exact API (do not re-guess or re-derive — confirmed via `javap` against the actual jars in this project's `~/.m2` cache):
  - `com.vaadin.flow.router.Location(String)` — public constructor, `throws com.vaadin.flow.router.InvalidLocationException`.
  - `com.vaadin.flow.router.ErrorParameter<T>(Class<T>, Exception)` — public constructor.
  - `com.vaadin.flow.router.HasErrorParameter<T>.setErrorParameter(BeforeEnterEvent, ErrorParameter<T>)` returns `int`.
  - `com.vaadin.flow.spring.security.VaadinSecurityConfigurer.anyRequest(Consumer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl>)` returns `VaadinSecurityConfigurer` (chainable).
  - `org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizedUrl.permitAll()` — the method the docs' recommended fix uses.

---

### Task 1: `RouteNotFoundView` — the custom not-found page

**Files:**
- Create: `src/main/java/com/pdg/adventure/view/error/RouteNotFoundView.java`
- Test: `src/test/java/com/pdg/adventure/view/error/RouteNotFoundViewTest.java`

**Interfaces:**
- Consumes: `com.pdg.adventure.view.RootView` (existing, `@Route(value = "")`, no route parameters) as the "Back to safety" navigation target.
- Produces: `public class RouteNotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException>` — no public API beyond the `HasErrorParameter` contract; Task 2 does not call anything on this class directly (it's wired in automatically by Vaadin's router since it implements `HasErrorParameter<NotFoundException>`), it only depends on this class *existing and being annotated correctly*.

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/pdg/adventure/view/error/RouteNotFoundViewTest.java`:

```java
package com.pdg.adventure.view.error;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.InvalidLocationException;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteNotFoundViewTest {

    @Mock
    private BeforeEnterEvent beforeEnterEvent;

    private RouteNotFoundView view;

    @BeforeEach
    void setUp() {
        view = new RouteNotFoundView();
    }

    @Test
    void view_isAnnotatedAnonymousAllowed() {
        assertThat(RouteNotFoundView.class.isAnnotationPresent(AnonymousAllowed.class)).isTrue();
    }

    @Test
    void view_hasNoParentLayoutAnnotation() {
        assertThat(RouteNotFoundView.class.isAnnotationPresent(com.vaadin.flow.router.ParentLayout.class)).isFalse();
    }

    @Test
    void setErrorParameter_returnsNotFoundStatus() throws InvalidLocationException {
        when(beforeEnterEvent.getLocation()).thenReturn(new Location("some/bad/path"));

        int status = view.setErrorParameter(beforeEnterEvent,
                new ErrorParameter<>(NotFoundException.class, new NotFoundException()));

        assertThat(status).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void setErrorParameter_messageContainsAttemptedPath() throws InvalidLocationException {
        when(beforeEnterEvent.getLocation()).thenReturn(new Location("some/bad/path"));

        view.setErrorParameter(beforeEnterEvent,
                new ErrorParameter<>(NotFoundException.class, new NotFoundException()));

        Paragraph message = view.getChildren()
                .filter(Paragraph.class::isInstance).map(Paragraph.class::cast)
                .findFirst().orElseThrow();
        assertThat(message.getText()).contains("some/bad/path")
                .contains("There's no path here");
    }

    @Test
    void view_containsPuzzledDragonImageWithAltText() {
        Image image = view.getChildren()
                .filter(Image.class::isInstance).map(Image.class::cast)
                .findFirst().orElseThrow();
        assertThat(image.getSrc()).contains("main_puzzled.jpg");
        assertThat(image.getAlt()).isPresent().get()
                .isEqualTo("A giant puzzled dragon towers over a small armored knight");
    }

    @Test
    void view_containsBackToSafetyButton() {
        Button button = view.getChildren()
                .filter(Button.class::isInstance).map(Button.class::cast)
                .findFirst().orElseThrow();
        assertThat(button.getText()).isEqualTo("Back to safety");
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run (from `server/`): `mvn -Dstyle.color=never test -Dtest='RouteNotFoundViewTest'`
Expected: build/compile FAILURE — `cannot find symbol: class RouteNotFoundView` (the class doesn't exist yet). This is the expected RED state for a brand-new class.

- [ ] **Step 3: Create the view**

Create `src/main/java/com/pdg/adventure/view/error/RouteNotFoundView.java`:

```java
package com.pdg.adventure.view.error;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;

import com.pdg.adventure.view.RootView;

/**
 * Standalone (no parent layout) so it renders for anonymous and authenticated
 * visitors alike — AdventuresMainLayout is @PermitAll and would otherwise
 * re-block anonymous access at the layout level.
 */
@AnonymousAllowed
public class RouteNotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    private final Paragraph message = new Paragraph();

    public RouteNotFoundView() {
        setSpacing(false);

        Image img = new Image("images/main_puzzled.jpg",
                "A giant puzzled dragon towers over a small armored knight");
        img.setWidth("300px");
        add(img);

        add(new H2("Even the dragon is puzzled."));
        add(message);

        Button backButton = new Button("Back to safety",
                e -> UI.getCurrent().navigate(RootView.class));
        add(backButton);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        message.setText("There's no path here — '" + event.getLocation().getPath()
                + "' doesn't lead anywhere in this world.");
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn -Dstyle.color=never test -Dtest='RouteNotFoundViewTest'`
Expected: PASS (`Tests run: 6, Failures: 0`).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/view/error/RouteNotFoundView.java src/test/java/com/pdg/adventure/view/error/RouteNotFoundViewTest.java
git commit -m "feat: add custom route-not-found view with puzzled-dragon page

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: Relax the Spring Security catch-all so the not-found view can render

**Files:**
- Modify: `src/main/java/com/pdg/adventure/config/SecurityConfig.java:1-25` (imports), `:83-86` (Vaadin configurer block)

**Interfaces:**
- Consumes: `RouteNotFoundView` from Task 1 — not called directly (Vaadin auto-discovers it as the `NotFoundException` handler at startup because it implements `HasErrorParameter<NotFoundException>`); this task only needs the class to exist and be correctly annotated, which Task 1 already guaranteed via its own tests.
- Produces: nothing further downstream — this is the last task in this plan.

There is no automated test for this change: this codebase's own testing conventions (`AGENTS-testing.md`) explicitly avoid full-Spring-context (`@SpringBootTest`/`SpringBrowserlessTest`) tests for views, because the test `application.properties` only configures MongoDB and a real filter-chain test would need MySQL/JPA too. Verification here is a manual smoke test against the running app, mirroring the exact repro from the UX audit.

- [ ] **Step 1: Add the import**

In `src/main/java/com/pdg/adventure/config/SecurityConfig.java`, add this import alongside the existing Spring Security imports (near line 15, after `import org.springframework.security.config.annotation.web.builders.HttpSecurity;`):

```java
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizedUrl;
```

- [ ] **Step 2: Relax the catch-all**

Find this block (currently lines 83-86):

```java
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
//                    , "/logged-out.html");
        });
```

Replace it with:

```java
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class)
                    .anyRequest(AuthorizedUrl::permitAll);
//                    , "/logged-out.html");
        });
```

- [ ] **Step 3: Confirm it compiles**

Run: `mvn -q -f /Users/mafw/workroom/projects/adventurebuilder/server/pom.xml -Dvaadin.skip.frontend.build=true compile`
Expected: no output, exit code 0 (this project's PostToolUse hook already runs this automatically after every edit — this step just confirms the result before moving on).

- [ ] **Step 4: Manual verification against the running app**

If the app isn't already running on `:8080`, start it (project's `run` skill, or `mvn spring-boot:run` from `server/`; requires MySQL and MongoDB to already be up — this is an environment prerequisite, not part of this change).

Run each of these and record the status code:

```bash
curl -s -o /dev/null -w "anonymous bad-url: %{http_code}\n" http://localhost:8080/this/route/does-not-exist
```
Expected: `404` (previously `302` redirect to `/login` — anonymous visitors now see the not-found page directly instead of being sent to login for a page that wouldn't exist even after logging in).

```bash
curl -s -o /dev/null -w "anonymous known-good route still redirects to login: %{http_code}\n" http://localhost:8080/admin/users
```
Expected: `302` (unchanged — a real, role-gated route for an unauthenticated visitor still correctly requires login; only *unmatched* routes changed behavior).

Log in as `admin`/`admin123` in a browser, open dev tools to confirm the session cookie, then:

```bash
curl -s -o /dev/null -w "authenticated bad-url: %{http_code}\n" -b "JSESSIONID=<paste from browser dev tools>" http://localhost:8080/this/route/does-not-exist
```
Expected: `404` with a real HTML body containing "Even the dragon is puzzled" (previously an empty `403`).

In the browser itself (not curl), navigate to a bad URL (e.g. `http://localhost:8080/blahblah`) while logged in as admin:
- Expected: the dragon page renders in the browser — no download prompt.
- Click "Back to safety": expected to land on the admin dashboard (confirms `RootView`'s role-based redirect still works from this entry point).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/pdg/adventure/config/SecurityConfig.java
git commit -m "fix: relax Vaadin security catch-all so unmatched routes reach the not-found view

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

## Self-Review Notes

**Spec coverage:** Both spec requirements (relax the catch-all; add an annotated, standalone not-found view with the approved copy and image) are covered — Task 1 builds and unit-tests the view in full isolation, Task 2 makes the one-line security change and closes the loop with the exact manual repro from the audit. The design doc's "Non-Goals" (role-mismatch 403 pages, `AdventuresMainLayout` changes) are correctly left untouched — no task touches either.

**Placeholder scan:** No TBD/TODO; every step has literal, complete code or exact commands with expected output.

**Type consistency:** `RouteNotFoundView` (Task 1) is referenced by exact name in Task 2's rationale and nowhere else in code — Task 2 makes no compile-time reference to it (Vaadin discovers it via classpath scanning for `HasErrorParameter` implementations), so there's no signature drift to check between tasks.
