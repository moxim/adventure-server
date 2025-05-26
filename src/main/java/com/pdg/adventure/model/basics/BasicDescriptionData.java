package com.pdg.adventure.model.basics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.pdg.adventure.model.Word;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BasicDescriptionData extends BasicData {
    @DBRef
    private Word adjective;
    @DBRef
    private Word noun;

    public BasicDescriptionData() {
        adjective = new Word("", Word.Type.ADJECTIVE);
        adjective.setId("666");
        noun = new Word("", Word.Type.NOUN);
        noun.setId("666");
    }
}
