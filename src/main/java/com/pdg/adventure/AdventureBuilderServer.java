package com.pdg.adventure;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot pdg.
 * Use the @PWA annotation make the pdg installable on phones, tablets
 * and some desktop browsers.
 */

@SpringBootApplication
//@EnableMongoRepositories(basePackages = "com.pdg.adventure.server.storage")
//@Theme(value = "adventureBuilder")
//@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.1.3")
@PWA(name = "Adventure Builder", shortName = "Adventure",
        offlineResources = {"./images/adventure.png"},
        offlinePath = "offline.html")
@StyleSheet(Lumo.STYLESHEET)
// The fonts an author can pick for running an adventure (AdventureFont). Only declares them: the
// browser downloads a font file when something actually uses it.
@StyleSheet("styles/adventure-fonts.css")
@ColorScheme(ColorScheme.Value.LIGHT_DARK)
public class AdventureBuilderServer implements AppShellConfigurator
        // extends SpringBootServletInitializer
{
    public static void main(String[] args) {
        //        LaunchUtil.launchBrowserInDevelopmentMode(
        SpringApplication.run(AdventureBuilderServer.class, args)
        //        )
        ;
    }
}
