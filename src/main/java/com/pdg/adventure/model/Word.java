package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.pdg.adventure.model.basics.BasicData;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
@Document(collection = "wordData")
public class Word extends BasicData {
    private String text;
    private Type type;
    // TODO: using DBRef doesn't resolve the synonym
    @DBRef//(lazy = true)
//    @CascadeSave
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
