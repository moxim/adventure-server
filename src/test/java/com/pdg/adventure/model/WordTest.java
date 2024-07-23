package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import static com.mongodb.assertions.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class WordTest {

    @Test
    void getSynonym() {
        Word get = new Word("get", Word.Type.VERB);
        Word take = new Word("take", get);
        Word obtain = new Word("obtain", take);
        Word fetch = new Word("fetch", obtain);

        // make sure that the synonym of a synonym is the same as the original word
        assertSame(get, take.getSynonym());
        assertSame(get, obtain.getSynonym());

        // make sure that there is no synonym chain
        assertSame(get, fetch.getSynonym());

        assertSame(get.getType(), fetch.getType());

        // make sure that the synonym of a word without a synonym is null
        assertNull(get.getSynonym());
    }
}
