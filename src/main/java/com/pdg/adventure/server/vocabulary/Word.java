package com.pdg.adventure.server.vocabulary;

public class Word {

    public enum WordType {
        VERB,
        NOUN,
        ADJECTIVE
    }

    private static int idCounter = 0;

    private final Integer id;

    private final String text;

    private final WordType type;

    public Word(String aWord, WordType aType) {
        id = idCounter++;
        text = aWord;
        type = aType;
    }

    public String getText() {
        return text;
    }

    public WordType getType() {
        return type;
    }

    @Override
    public boolean equals(Object aO) {
        if (this == aO) return true;
        if (!(aO instanceof Word word)) return false;

        return id.equals(word.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
            return text + "[" + type + "]";
        }
}
