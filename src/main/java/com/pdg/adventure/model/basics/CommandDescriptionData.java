package com.pdg.adventure.model.basics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.model.Word;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class CommandDescriptionData extends BasicDescriptionData {
    @DBRef
    private Word verb = new Word("", Word.Type.VERB);

    public CommandDescriptionData(String aCommandSpec) {
        setCommandSpecification(aCommandSpec);
    }

    public CommandDescriptionData() {
        // default constructor
    }

    public String getCommandSpecification() {
        String verb = getVerb() == null ? "" : getVerb().getText();
        String adjective = getAdjective() == null ? "" : getAdjective().getText();
        String noun = getNoun() == null ? "" : getNoun().getText();

        return verb + CommandDescription.COMMAND_SEPARATOR + adjective + CommandDescription.COMMAND_SEPARATOR + noun;
    }

    public void setCommandSpecification(String aCommandSpec) {
        if (aCommandSpec == null || aCommandSpec.isEmpty()) {
            throw new IllegalArgumentException("Command spec must have 3 parts: " + aCommandSpec);
        }

        String [] parts = (aCommandSpec + " ").split("\\" + CommandDescription.COMMAND_SEPARATOR);
        if (parts.length > 2) {
            setVerb(new Word(parts[0], Word.Type.VERB));
            setAdjective(new Word(parts[1], Word.Type.ADJECTIVE));
            setNoun(new Word(parts[2].trim(), Word.Type.NOUN));
        } else {
            throw new IllegalArgumentException("Command spec must have 3 parts: " + aCommandSpec);
        }

    }
}
