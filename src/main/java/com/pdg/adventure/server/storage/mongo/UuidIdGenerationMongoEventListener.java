package com.pdg.adventure.server.storage.mongo;

import com.github.f4b6a3.ulid.Ulid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.pdg.adventure.api.Ided;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // Runs before cascade listener (default order 0)
public class UuidIdGenerationMongoEventListener extends AbstractMongoEventListener<Ided> {
    Logger LOG = LoggerFactory.getLogger(UuidIdGenerationMongoEventListener.class);

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Ided> event) {
        LOG.debug("UuidIdGenerationMongoEventListener.onBeforeConvert: {}",
                  event.getSource().getClass().getSimpleName());
        Ided source = event.getSource();
        if (source.getId() == null || source.getId().isEmpty()) {
            final String id = Ulid.fast().toString();
            LOG.debug("Assigning new id: {} to {}", id, source.getClass().getSimpleName());
            source.setId(id);
        }
    }
}
