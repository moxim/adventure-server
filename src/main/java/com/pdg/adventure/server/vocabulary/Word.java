package com.pdg.adventure.server.vocabulary;

import java.util.UUID;

import com.pdg.adventure.api.Ided;

public class Word implements Ided {
    private String id;
    private final String text;
    private final Type type;

    Word(String aText, Type aType) {
        id = UUID.randomUUID().toString();
        text = aText;
        type = aType;
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return text + "[" + type + "]";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String anId) {
        id = anId;
    }

    public enum Type {
        VERB,
        NOUN,
        ADJECTIVE
    }
}
