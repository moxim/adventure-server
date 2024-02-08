package com.pdg.adventure.model.basics;

import com.pdg.adventure.model.Word;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BasicDescriptionData extends BasicData {
    @DBRef
    private Word adjective = new Word("", Word.Type.ADJECTIVE);
    @DBRef
    private Word noun = new Word("", Word.Type.NOUN);;
}
