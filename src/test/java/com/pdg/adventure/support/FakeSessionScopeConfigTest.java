package com.pdg.adventure.support;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import static org.assertj.core.api.Assertions.assertThat;

class FakeSessionScopeConfigTest {

    @Scope(value = FakeSessionScope.NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public static class Counter {
        private int count;

        public int next() {
            return ++count;
        }
    }

    @Test
    void eachFakeSessionGetsItsOwnBeanBehindOneProxy() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(FakeSessionScopeConfig.class, Counter.class);
            context.refresh();
            Counter counter = context.getBean(Counter.class);
            FakeSessionScope sessions = context.getBean(FakeSessionScope.class);

            sessions.useSession("a");
            counter.next();
            counter.next();
            sessions.useSession("b");
            assertThat(counter.next()).isEqualTo(1);
            sessions.useSession("a");
            assertThat(counter.next()).isEqualTo(3);
        }
    }
}
