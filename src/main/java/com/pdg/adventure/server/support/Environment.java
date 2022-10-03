package com.pdg.adventure.server.support;

public class Environment {

    private Environment() {
        // don't instantiate me
    }

    public static void tell(String aMessage) {
        System.out.println(aMessage);
    }
}
