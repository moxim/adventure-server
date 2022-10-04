package com.pdg.adventure.server.api;

public interface Command {

    String getDescription();

    void execute();
}
