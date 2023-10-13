package com.pdg.adventure.model;

import org.junit.jupiter.api.Test;

import static com.mongodb.assertions.Assertions.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class WordTest {

    @Test
    void getSynonym() {
        Word get = new Word("get", Word.Type.VERB);
        Word take = new Word("take", get);
        Word obtain = new Word("obtain", take);
        assertSame(get, take.getSynonym());
        assertSame(get, obtain.getSynonym());
        assertSame(get.getType(), obtain.getType());
        assertEquals(get.getType(), obtain.getType());
        assertNull(get.getSynonym());
    }
}
