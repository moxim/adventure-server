package com.pdg.adventure.model.basics;

import com.pdg.adventure.model.Word;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class CommandDescriptionData extends BasicDescriptionData {
    @DBRef
    private Word verb = new Word("", Word.Type.VERB);;
}
