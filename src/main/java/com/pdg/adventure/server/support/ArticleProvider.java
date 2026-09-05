package com.pdg.adventure.server.support;

public class ArticleProvider {

    private ArticleProvider() {
        // don't instantiate me
    }

    public static String stripLeadingArticlesAndPronouns(String aText) {
        return stripLeadingArticles(stripLeadingPronouns(aText));
    }

    public static String prependIndefiniteArticle(String aText) {
        String strippedText = stripLeadingArticles(stripLeadingPronouns(aText));

        final char firstChar = strippedText.charAt(0);
        return switch (firstChar) {
            case 'a', 'e', 'i', 'o', 'u' -> "an " + strippedText;
            default -> "a " + strippedText;
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

    private static String stripLeadingArticles(String aText) {
        String[] articles = {"a ", "an ", "the ", "some "};
        for (String article : articles) {
            if (aText.startsWith(article)) {
                return aText.substring(article.length());
            }
        }
        return aText;
    }

    private static String stripLeadingPronouns(String aText) {
        String[] pronouns = {"my ", "your ", "his ", "her ", "its ", "our ", "their "};
        for (String pronoun : pronouns) {
            if (aText.startsWith(pronoun)) {
                return aText.substring(pronoun.length());
            }
        }
        return aText;
    }

    public static String prependDefiniteArticle(String aText) {
        String strippedText = stripLeadingArticles(stripLeadingPronouns(aText));
        return "the " + strippedText;
    }
}
