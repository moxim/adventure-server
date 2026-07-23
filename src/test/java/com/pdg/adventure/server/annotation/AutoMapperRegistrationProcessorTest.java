package com.pdg.adventure.server.annotation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.mapper.action.MessageActionMapper;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Reproduces a real Spring context boot (no mocks) for the mapper auto-registration machinery.
 * AutoMapperRegistrationProcessor is itself a BeanPostProcessor that depends on MapperSupporter,
 * which forces MapperSupporter to be created during Spring's early BeanPostProcessor-registration
 * phase - before AutoMapperRegistrationProcessor has finished being registered as an active
 * BeanPostProcessor. If the registration flush is triggered from
 * postProcessBeforeInitialization(mapperSupporterBean), that trigger never fires, and every
 * @AutoRegisterMapper mapper (all of which depend on MapperSupporter via their abstract base
 * classes, so they're all constructed even later, in the regular singleton phase) is queued but
 * never flushed into the registry.
 */
class AutoMapperRegistrationProcessorTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void realSpringContext_registersAutoRegisterMapperBeans_soMapperSupporterCanFindThem() {
        context = new AnnotationConfigApplicationContext();
        context.register(GameContext.class, AdventureConfig.class, MapperSupporter.class,
                         AutoMapperRegistrationProcessor.class, MessageActionMapper.class);
        context.refresh();

        MapperSupporter mapperSupporter = context.getBean(MapperSupporter.class);

        assertThat(mapperSupporter.getMapper(MessageActionData.class)).isNotNull();
    }
}
