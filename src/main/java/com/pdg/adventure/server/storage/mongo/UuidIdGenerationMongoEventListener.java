package com.pdg.adventure.server.storage.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

import com.pdg.adventure.api.Ided;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // Runs before cascade listener (default order 0)
public class UuidIdGenerationMongoEventListener extends AbstractMongoEventListener<Ided> {
    Logger LOG = LoggerFactory.getLogger(UuidIdGenerationMongoEventListener.class);

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Ided> event) {
        LOG.debug("UuidIdGenerationMongoEventListener.onBeforeConvert: {}", event.getSource().getClass().getSimpleName());
        Ided source = event.getSource();
        if (source.getId() == null || source.getId().isEmpty()) {
            final String id = UUID.randomUUID().toString();
            LOG.debug("Assigning new UUID id: {} to {}", id, source.getClass().getSimpleName());
            source.setId(id);
        } else {
            LOG.debug("Existing UUID id found: {}", source.getId());
        }
    }
}
