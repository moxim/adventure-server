package com.pdg.adventure.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot pdg.
 *
 * Use the @PWA annotation make the pdg installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
public class AdventureBuilderServer
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
