package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.annotation.AutoMapperRegistrationProcessor;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.vocabulary.Vocabulary;

@ExtendWith(SpringExtension.class)
@Import({AdventureConfig.class, MapperSupporter.class, VocabularyMapper.class, DescriptionMapper.class, AutoMapperRegistrationProcessor.class})
class AutoMapperRegistrationTest {

    // AdventureConfig has a @Lazy GameContext constructor parameter; a mock satisfies the dependency
    // without pulling in the full application context or JPA infrastructure.
    @MockitoBean
    GameContext gameContext;

    @Autowired
    private MapperSupporter mapperSupporter;

    @Autowired
    private VocabularyMapper vocabularyMapper;

    @Autowired
    private DescriptionMapper descriptionMapper;

    @Test
    void testVocabularyMapperRegistration() {
        Mapper<VocabularyData, Vocabulary> mapper = vocabularyMapper;

        assertNotNull(mapper, "VocabularyMapper should be auto-registered");
    }

    @Test
    void testDescriptionMapperRegistration() {
        Mapper<DescriptionData, DescriptionProvider> mapper = descriptionMapper;

        assertNotNull(mapper, "DescriptionMapper should be auto-registered");
    }

    @Test
    void testMapperSupporterInitialization() {
        assertNotNull(mapperSupporter, "MapperSupporter should be properly injected");
        assertNotNull(mapperSupporter.getVocabulary(), "Vocabulary should be available");
    }
}
