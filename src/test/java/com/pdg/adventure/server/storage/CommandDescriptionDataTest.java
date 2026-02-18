package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.storage.mongo.CascadeSaveMongoEventListener;
import com.pdg.adventure.server.storage.mongo.UuidIdGenerationMongoEventListener;

@DataMongoTest
@Import(value = {UuidIdGenerationMongoEventListener.class, CascadeSaveMongoEventListener.class,
                 de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommandDescriptionDataTest {

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testSomething(@Autowired MongoTemplate mongoTemplate) {
        CommandDescriptionData command = new CommandDescriptionData();

        Word verbWord = new Word("go", Word.Type.VERB);
        mongoTemplate.save(verbWord);
        Word nounWord = new Word("room", Word.Type.NOUN);
        mongoTemplate.save(nounWord);

        command.setVerb(verbWord);
        command.setNoun(nounWord);
        command.setAdjective(null); // Should exclude adjective from document

        final CommandDescriptionData save = mongoTemplate.save(command);
        List<CommandDescriptionData> commands = mongoTemplate.findAll(CommandDescriptionData.class);
        assertThat(commands).hasSize(1);

        final CommandDescriptionData savedCommandDescription = commands.getFirst();
        assertThat(savedCommandDescription.getId()).isEqualTo(save.getId());
        assertThat(savedCommandDescription.getVerb().getText()).isEqualTo("go");
        assertThat(savedCommandDescription.getNoun().getText()).isEqualTo("room");
        assertThat(savedCommandDescription.getAdjective()).isNull();
    }
}
