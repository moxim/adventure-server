package com.pdg.adventure.server.engine;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a bean to the Vaadin session (the same "vaadin-session" scope as {@code @VaadinSessionScope}) and injects
 * a scoped proxy, so singletons can hold the bean and still reach the calling browser session's instance on every
 * call. {@code @VaadinSessionScope} itself has no proxy mode, hence this composed annotation.
 * <p>
 * Beans with this scope can only be used on a thread that has a bound VaadinSession (UI event handlers, not
 * background threads).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public @interface PerBrowserSession {
}
