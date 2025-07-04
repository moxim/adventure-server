package com.pdg.adventure;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot pdg.
 * Use the @PWA annotation make the pdg installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "adventureBuilder")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.1.3")
@PWA(name = "Adventure Builder", shortName = "Adventure",
        offlineResources = { "./images/adventure.png"},
        offlinePath = "offline.html")
public class AdventureBuilderServer implements AppShellConfigurator
        // extends SpringBootServletInitializer
{
    public static void main(String[] args) {
        //        LaunchUtil.launchBrowserInDevelopmentMode(
        SpringApplication.run(AdventureBuilderServer.class, args)
        //        )
        ;
        //        MiniAdventure.main(args);
    }
}
