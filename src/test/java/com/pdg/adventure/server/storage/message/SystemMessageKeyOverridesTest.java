package com.pdg.adventure.server.storage.message;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemMessageKeyOverridesTest {

    private static final String ID = SystemMessageKey.SM9.id();
    private static final String BUILT_IN = SystemMessageKey.SM9.defaultText();

    @Test
    void unbound_returnsTheBuiltInText() {
        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void bound_returnsTheOverride_andRestoresTheBuiltInTextOnClose() {
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of(ID, "Carrying:"))) {
            assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo("Carrying:");
        }

        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void aKeyWithoutAnOverride_keepsItsBuiltInTextWhileOthersAreBound() {
        String builtIn8 = SystemMessageKey.SM8.defaultText();

        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of(ID, "Carrying:"))) {
            assertThat(SystemMessageKey.SM8.defaultText()).isEqualTo(builtIn8);
        }
    }

    @Test
    void nestedBindings_restoreTheOuterOneWhenTheInnerCloses() {
        try (SystemMessageKey.Binding outer = SystemMessageKey.bindOverrides(Map.of(ID, "outer"))) {
            try (SystemMessageKey.Binding inner = SystemMessageKey.bindOverrides(Map.of(ID, "inner"))) {
                assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo("inner");
            }
            assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo("outer");
        }

        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void closingAfterAnException_stillUnbinds() {
        assertThatThrownBy(() -> {
            try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of(ID, "Carrying:"))) {
                throw new IllegalStateException("boom");
            }
        }).isInstanceOf(IllegalStateException.class);

        assertThat(SystemMessageKey.SM9.defaultText()).isEqualTo(BUILT_IN);
    }

    @Test
    void aBinding_isInvisibleToOtherThreads() throws Exception {
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of(ID, "Carrying:"))) {
            AtomicReference<String> seenByOtherThread = new AtomicReference<>();
            Thread other = new Thread(() -> seenByOtherThread.set(SystemMessageKey.SM9.defaultText()));
            other.start();
            other.join();

            assertThat(seenByOtherThread.get()).isEqualTo(BUILT_IN);
        }
    }
}
