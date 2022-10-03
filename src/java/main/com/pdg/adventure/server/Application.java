package com.pdg.adventure.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
//@Theme(value = "adventurebuilder")
//@PWA(name = "AdventureBuilder", shortName = "AdventureBuilder", offlineResources = {"images/logo.png"})
//@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application
// extends SpringBootServletInitializer      implements AppShellConfigurator
{

    public static void main(String[] args) {
//        LaunchUtil.launchBrowserInDevelopmentMode(
                SpringApplication.run(Application.class, args)
//        )
        ;
    }

}
