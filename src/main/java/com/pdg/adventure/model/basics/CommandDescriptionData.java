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
    private Word verb;

    public CommandDescriptionData() {
        // Default constructor
    }

    public CommandDescriptionData(String aCommandSpec) {
        setCommandSpecification(aCommandSpec);
    }

    public String getCommandSpecification() {
        String verbText = verb != null && verb.getText() != null ? verb.getText() : "";
        String adjectiveText = getAdjective() != null && getAdjective().getText() != null ? getAdjective().getText() : "";
        String nounText = getNoun() != null && getNoun().getText() != null ? getNoun().getText() : "";
        StringBuilder result = new StringBuilder();
//        if (!verbText.isEmpty()) {
            result.append(verbText);
//        }
//        if (!adjectiveText.isEmpty()) {
//            if (result.length() > 0)
                result.append(CommandDescription.COMMAND_SEPARATOR);
            result.append(adjectiveText);
//        }
//        if (!nounText.isEmpty()) {
//            if (result.length() > 0)
                result.append(CommandDescription.COMMAND_SEPARATOR);
            result.append(nounText);
//        }
        return result.toString();
    }

    public void setCommandSpecification(String aCommandSpec) {
        if (aCommandSpec == null || aCommandSpec.trim().isEmpty()) {
            return; // Allow empty input to reset fields
        }

        String[] parts = (aCommandSpec).split("\\" + CommandDescription.COMMAND_SEPARATOR, 3);
        setVerb(parts[0].trim().isEmpty() ? null : new Word(parts[0].trim(), Word.Type.VERB));
        setAdjective(parts[1].trim().isEmpty() ? null : new Word(parts[1].trim(), Word.Type.ADJECTIVE));
        setNoun(parts[2].trim().isEmpty() ? null : new Word(parts[2].trim(), Word.Type.NOUN));
    }
}
