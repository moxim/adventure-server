package com.pdg.adventure.config;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;

import com.pdg.adventure.security.model.Role;
import com.pdg.adventure.view.login.LoginView;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${security.remember-me.key}")
    private String rememberMeKey;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /**
         * Delegating the responsibility of general configuration
         * of HTTP security to the VaadinSecurityConfigurer.
         *
         * It's configuring the following:
         * - Vaadin's CSRF protection by ignoring internal framework requests,
         * - default request cache,
         * - ignoring public views annotated with @AnonymousAllowed,
         * - restricting access to other views/endpoints, and
         * - enabling ViewAccessChecker authorization.
         */

        // RootView handles all post-login routing, so we never want Spring Security
        // to save a URL and redirect back to it after login.
        http.requestCache(cache -> cache.requestCache(new NullRequestCache()));

        // Single Session per UserData
        http.sessionManagement(session ->
            session.maximumSessions(1).maxSessionsPreventsLogin(false)
        );

        http.rememberMe(customizer -> customizer.key(rememberMeKey).alwaysRemember(false));

        // Logout
        http.logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        // Configure role-based URL access before calling VaadinSecurityConfigurer.vaadin()
        // as it adds a final anyRequest matcher.
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/admin/**").hasAnyRole(Role.ADMIN.name())
                .requestMatchers("/author/**").hasAnyRole(Role.AUTHOR.name())
                .requestMatchers("/player/**").hasAnyRole(Role.PLAYER.name())
                .requestMatchers("/public/**").permitAll();
            // Permit "/" so an anonymous visit to the root is never saved as a redirect
            // target by ExceptionTranslationFilter and bounced back after login.
            auth.requestMatchers(
                    "/login", "/",
                     "/VAADIN/**",
                    "/favicon.ico",
                    "/robots.txt",
                    "/manifest.webmanifest",
                    "/sw.js",
                    "/offline.html",
                    "/icons/**",
                    "/images/**",
                    "/styles/**",
                    "/frontend/**").permitAll();
        });

        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
//                    , "/logged-out.html");
        });

        // Keep only the failure handler for login-attempt logging.
        // The success handler is intentionally left to VaadinSecurityConfigurer so that
        // its internal session/navigation setup is not bypassed (overriding it caused a
        // configurer ordering conflict that redirected users back to the login page).
        http.formLogin(form -> {
            form.loginPage("/login").permitAll();
            form.failureHandler(customFailureHandler());
        });

          return http.build();
    }

    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return (request, response, exception) -> {
            LOG.info("Login failed for user '{}': {}", request.getParameter("username"), exception.getMessage());
            response.sendRedirect("/login?error");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Role hierarchy: ADMIN inherits AUTHOR permissions, AUTHOR inherits PLAYER permissions.
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_ADMIN > ROLE_AUTHOR
                ROLE_AUTHOR > ROLE_PLAYER
                """);
    }
}
