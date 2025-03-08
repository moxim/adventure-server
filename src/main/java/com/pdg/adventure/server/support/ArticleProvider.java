package com.pdg.adventure.server.support;

public class ArticleProvider {
    private ArticleProvider() {
        // don't instantiate me
    }

    public static String prependIndefiniteArticle(String aText) {
        if (aText == null || aText.isEmpty()) {
            return aText;
        }

        if (startsWithPronoun(aText)) {
            return aText;
        }

        if (startsWithArticle(aText)) {
            return aText;
        }

        final char firstChar = aText.charAt(0);
        return switch (firstChar) {
            case 'a', 'e', 'i', 'o', 'u' : yield "an " + aText;
            default: yield "a " + aText;
        };
    }

    private static boolean startsWithArticle(String aText) {
        String[] articles = {"a ", "an ", "the ", "some "};
        for (String article : articles) {
            if (aText.startsWith(article)) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWithPronoun(String aText) {
        String[] pronouns = {"my ", "your ", "his ", "her ", "its ", "our ", "their "};
        for (String pronoun : pronouns) {
            if (aText.startsWith(pronoun)) {
                return true;
            }
        }
        return false;
    }

    public static String prependDefiniteArticle(String aText) {
        if (startsWithArticle(aText)) {
            return aText;
        }
        if (startsWithPronoun(aText)) {
            return aText;
        }
        return "the " + aText;
    }
}
