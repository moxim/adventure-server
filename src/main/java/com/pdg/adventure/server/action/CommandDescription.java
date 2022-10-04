package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Describable;

public class CommandDescription {
    private final String verb;
    private final String adjective;
    private final String noun;

    public CommandDescription (String aVerb, Describable aNamedThing) {
        this(aVerb, aNamedThing.getAdjective(), aNamedThing.getNoun());
    }

    public CommandDescription(String aVerb) {
        this(aVerb, "", "");
    }

    public CommandDescription(String aVerb, String aNoun) {
        this(aVerb, "", aNoun);
    }

    public CommandDescription(String aVerb, String anAdjective, String aNoun) {
        verb = aVerb;
        adjective = anAdjective;
        noun = aNoun;
    }

    public String getDescription() {
        return verb + "_" + adjective + "_" + noun;
    }
}
