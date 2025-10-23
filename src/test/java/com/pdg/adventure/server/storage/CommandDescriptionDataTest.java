package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;
import com.pdg.adventure.view.support.ViewSupporter;

@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommandDescriptionDataTest {

    @Test
    @DirtiesContext(methodMode=DirtiesContext.MethodMode.BEFORE_METHOD)
    public void testSomething(@Autowired MongoTemplate mongoTemplate) {
        CommandDescriptionData command = new CommandDescriptionData();
        command.setVerb(new Word("go", Word.Type.VERB));
        command.setNoun(new Word("room", Word.Type.NOUN));
        command.setAdjective(null); // Should exclude adjective from document
        final CommandDescriptionData save = mongoTemplate.save(command);
        List<CommandDescriptionData> commands = mongoTemplate.findAll(CommandDescriptionData.class);
        assertThat(commands.size() == 1);
        commands.forEach(commandDescription -> System.out.println(ViewSupporter.formatDescription(commandDescription)));
    }
}
