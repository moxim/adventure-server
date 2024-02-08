package com.pdg.adventure.model;

import com.pdg.adventure.model.basics.BasicData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
@Document(collection = "wordData")
public class Word extends BasicData {
    private String text;
    private Type type;
    // TODO: using DBRef doesn't resolve the synonym
    @DBRef//(lazy = true)
    private Word synonym;

    protected Word() {
    }

    public Word(String aText, Word aSynonym) {
        text = aText;
        Word syn = findRoot(aSynonym);
        if (syn != null) {
            synonym = syn;
            type = synonym.getType();
        }
    }

    public Word(String aText, Type aType) {
        text = aText;
        type = aType;
        synonym = null;
    }

    private Word findRoot(Word aWord) {
        if (null == aWord) {
            return null;
        }
        Word result = aWord; // word is not null, we just checked
        Word oSyn = aWord.getSynonym();
        while (oSyn != null) {
            result = oSyn;
            oSyn = oSyn.getSynonym();
        }
        return result;
    }

    public enum Type {
        VERB,
        NOUN,
        ADJECTIVE
    }
}
