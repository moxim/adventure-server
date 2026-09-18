package com.pdg.adventure.server.storage.mongo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class CascadeSaveMongoEventListenerTest {

    private final CascadeSaveMongoEventListener listener = new CascadeSaveMongoEventListener(
            mock(MongoTemplate.class), new UuidIdGenerationMongoEventListener());

    @Test
    @Timeout(5)
    void onBeforeConvert_doesNotStackOverflow_onCyclicEmbeddedObjectGraph() {
        // Two plain embedded (non-@DBRef) objects referencing each other - assignUuidsRecursively
        // must not recurse forever chasing this cycle.
        NodeA a = new NodeA();
        NodeB b = new NodeB();
        a.partner = b;
        b.partner = a;

        assertThatCode(() -> listener.onBeforeConvert(new BeforeConvertEvent<>(a, "test-collection")))
                .doesNotThrowAnyException();
    }

    private static class NodeA {
        NodeB partner;
    }

    private static class NodeB {
        NodeA partner;
    }
}
