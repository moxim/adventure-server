package com.pdg.adventure.server.support;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleProviderTest {
    @ParameterizedTest
    @ValueSource(strings = {"b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z"})
    void prependUnknownArticleForConsonants(String aConstant) {
        // given
        // when
        String test = ArticleProvider.prependIndefiniteArticle(aConstant + "text");

        // then
        assertThat(test).startsWith("a");
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "e", "i", "o", "u"})
    void prependUnknownArticleForVowels(String aVovwel) {
        // given
        // when
        String test = ArticleProvider.prependIndefiniteArticle(aVovwel + "word");

        // then
        assertThat(test).startsWith("an");
    }

    @ParameterizedTest
    @ValueSource(strings = {"my " , "your ", "his ", "her ", "its ", "our " , "their ", "a ", "an ", "the ", "some "})
    void prependUnknownArticleForPronounsOrArticles(String aPronoun) {
        // given
        // when
        String test = ArticleProvider.prependIndefiniteArticle(aPronoun + "thing");

        // then
        assertThat(test).startsWith(aPronoun);
    }
}
