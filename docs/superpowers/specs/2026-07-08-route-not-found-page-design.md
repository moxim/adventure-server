# Custom Route-Not-Found Page (UX Audit P1.3) — Design

## Problem

From the UX audit (`docs/ux-audit-2026-07-06.md`, project root, P1 finding #3): visiting an unknown URL in the running app (e.g. `/this/route/does-not-exist`) does not show a "page not found" message. Logged in, the server returns an empty HTTP 403 with no content-type, which Chrome interprets as a file download and shows a download prompt instead of any page content. There is no `RouteNotFoundError` customization anywhere in the codebase, and no route-not-found view test exists.

## Root Cause (verified against Vaadin 25.2 docs, not guessed)

Two independent Vaadin/Spring Security behaviors compound:

1. **`VaadinSecurityConfigurer`'s catch-all rule defaults to `denyAll()`.** In `SecurityConfig.java`, `http.authorizeHttpRequests(...)` declares explicit rules for `/admin/**`, `/author/**`, `/player/**`, `/public/**`, and a handful of static paths. Anything that matches none of those falls through to the Vaadin configurer's own implicit final matcher, which is `AuthorizedUrl::denyAll()` by default (per the "Vaadin Security Configurer" doc). This denial happens **at the HTTP layer, before Vaadin's router ever runs** — so a route-not-found view never even gets a chance to render.
   - This single rule explains BOTH previously-observed symptoms: an authenticated user hitting a bad URL is *authenticated but denied* → Spring returns a bare 403. An anonymous user hitting the same bad URL is *unauthenticated and denied* → Spring's `ExceptionTranslationFilter` treats that combination as "maybe logging in would help" and issues a 302 to `/login` instead. Same rule, two different surface behaviors depending on login state.
2. **Vaadin's built-in `RouteNotFoundError` has no access-control annotation.** Even if a request reaches Vaadin's router, `NavigationAccessControl` denies rendering any view (including the default not-found view) that lacks an explicit `@AnonymousAllowed`/`@PermitAll`/`@RolesAllowed` annotation. This produces a *second*, Vaadin-internal 403, independent of Spring's HTTP-layer check.

Fixing this needs both: relax the Spring catch-all so unmatched requests reach the router, AND provide a custom not-found view carrying an explicit access annotation.

**Source:** Vaadin 25.2 docs, "Router Exception Handling" (`/flow/routing/exceptions`) and "Vaadin Security Configurer" (`/flow/security/vaadin-security-configurer#configurer`), fetched and quoted directly — see plan implementation notes for the exact code samples.

## Fix Architecture

### 1. `SecurityConfig.java` — relax the catch-all

```java
http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
    configurer.loginView(LoginView.class)
            .anyRequest(AuthorizedUrl::permitAll);
});
```

The docs explicitly confirm this is safe: *"Relaxing the catch-all rule doesn't expose protected views: real routes are still guarded by the secured-route request matchers at the HTTP layer and by navigation access control at the navigation level."* Every existing `/admin/**`, `/author/**`, `/player/**` matcher is declared earlier in the same `authorizeHttpRequests` block and is evaluated first — this change only affects requests matching **none** of those, i.e. requests that don't correspond to any real, gated, or static content today.

**Side effect (intentional, and an improvement):** anonymous visitors hitting a bad URL will now see the not-found page directly instead of being redirected to `/login`. Today's redirect-to-login for a URL that wouldn't exist even after logging in is arguably more confusing than a direct "this doesn't exist."

**Explicit non-goal:** a **role-mismatch** 403 (e.g. a PLAYER visiting a mistyped `/author/...` URL) is unaffected — Spring denies that at the existing `/author/**` `hasRole` matcher, before the relaxed catch-all is ever reached. That's correct, existing behavior (info-hiding on role-gated sections) and a different problem from "the URL doesn't exist anywhere." A friendly access-denied page for that case is a reasonable future follow-up, out of scope here.

### 2. New view: `com.pdg.adventure.view.error.RouteNotFoundView`

- `extends VerticalLayout implements HasErrorParameter<NotFoundException>` — a normal Vaadin component tree (`Image`, headings, `Paragraph`, `Button`), not the bare-text `getElement().setText(...)` style of the minimal doc example, matching how the rest of this codebase's views are built (e.g. `AboutView`).
- `@AnonymousAllowed` — required so Vaadin's `NavigationAccessControl` allows the view to render for any visitor, logged in or not.
- **No `@ParentLayout`.** `AdventuresMainLayout` (the shell used by `AboutView` and most other views) is itself `@PermitAll`, and Vaadin checks the parent layout's access rule in addition to the view's own — nesting in it would silently re-block anonymous visitors, defeating the whole point. Instead this view is **standalone**, the same pattern `LoginView` already uses (`@Route(..., autoLayout = false)`, no shell) — it works identically regardless of login state.
- `setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter)` returns `HttpServletResponse.SC_NOT_FOUND` (404) and fills in the attempted path (`event.getLocation().getPath()`) into the message — matching the diagnostic detail the framework's own default provides, just phrased more kindly. Vaadin's `Paragraph`/`setText(...)` escapes this automatically, so reflecting the attempted URL back is not an XSS risk.
- Static parts (image, heading, button) built once in the constructor; the path-specific sentence is set inside `setErrorParameter`, since the path isn't known until then.

**Content** (approved copy, dragon-themed to match `AboutView`'s existing playful voice):
- Image: `images/main_puzzled.jpg` (the new dragon-and-knight illustration), alt text `"A giant puzzled dragon towers over a small armored knight"`.
- Heading: "Even the dragon is puzzled."
- Body: "There's no path here — '{attempted path}' doesn't lead anywhere in this world."
- Button: "Back to safety" — navigates to `RootView` (`/`), which already correctly routes admin/author/player to their own dashboard, or anonymous visitors to `/login`. No new routing logic needed; this view just needs to trigger navigation to `""`.
- Layout: centered (`setSizeFull()`, `JustifyContentMode.CENTER`, centered horizontal alignment), consistent with `AboutView`'s centered treatment even though this page has no shared shell.

## File Structure

| File | Change |
|---|---|
| `src/main/java/com/pdg/adventure/config/SecurityConfig.java` | Modify: add `.anyRequest(AuthorizedUrl::permitAll)` to the Vaadin configurer chain + new import |
| `src/main/java/com/pdg/adventure/view/error/RouteNotFoundView.java` | Create: the not-found view described above |
| `src/test/java/com/pdg/adventure/view/error/RouteNotFoundViewTest.java` | Create: browserless test |

## Testing Approach

**Browserless unit test** (`RouteNotFoundView`, no Spring context needed — it's a plain Vaadin component):
- `@AnonymousAllowed` is present on the class (reflection check — this is the crux of the whole fix, worth asserting directly rather than only indirectly).
- `setErrorParameter(...)` returns `HttpServletResponse.SC_NOT_FOUND`.
- The rendered content contains an `Image` referencing `main_puzzled.jpg`.
- The rendered message contains the attempted path (mock a `BeforeEnterEvent` whose `getLocation()` returns a real `Location` for a test path, e.g. `"some/bad/path"`).
- A button/link exists that would navigate back to root.

**Manual smoke verification** (documented as a plan step, run against the live app — this project's own testing conventions explicitly avoid full-Spring-context tests for views, and the security-filter-chain wiring can only really be exercised end-to-end):
1. `curl -s -i http://localhost:8080/this/route/does-not-exist` while logged out → expect `404` with a real HTML body (today: `302` to `/login`).
2. Same curl with a valid session cookie (logged in as admin) → expect `404` with a real HTML body (today: empty `403`).
3. Drive it in a real browser (Playwright, as originally used to find this bug) → confirm the dragon page renders instead of triggering a download prompt.
4. Sanity-check that a real, existing role-gated route (e.g. `/admin/users` as a non-admin) still returns a 403/access-denied outcome, unchanged — confirming the relaxed catch-all didn't loosen anything it shouldn't have.

## Non-Goals

- A friendly page for role-mismatch 403s (existing route, wrong role) — different bug, future follow-up.
- Any change to `AdventuresMainLayout`'s own access annotation.
- Handling for `NotFoundException` scenarios beyond plain unmatched URLs (e.g. malformed route parameters) — the existing `RouteNotFoundError` machinery already covers those the same way; this view simply replaces it as the resolver for `NotFoundException` app-wide.
