package com.pdg.adventure.server.support;

public class ArticleProvider {
    private ArticleProvider() {
        // don't instantiate me
    }

    public static String prependUnknownArticle(String aText) {
        final char firstChar = aText.charAt(0);
        return switch (firstChar) {
            case 'a', 'e', 'i', 'o', 'u' : yield "an " + aText;
            default: yield "a " + aText;
        };
    }
}
