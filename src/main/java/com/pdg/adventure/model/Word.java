package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.pdg.adventure.model.basic.DatedData;

@Document(collection = "words")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Word extends DatedData {
    private String text;
    private Type type;
    @DBRef
    private Word synonym;

    protected Word() {
    }

    public Word(String aText, Word aSynonym) {
        text = aText.toLowerCase();
        Word syn = aSynonym.getSynonym();
        if (syn != null) {
            synonym = syn;
        } else {
            synonym = aSynonym;
        }
        type = aSynonym.getType();
    }

    public Word(String aText, Type aType) {
        text = aText.toLowerCase();
        type = aType;
        synonym = null;
    }

    public enum Type {
        VERB,
        NOUN,
        ADJECTIVE
    }
}
