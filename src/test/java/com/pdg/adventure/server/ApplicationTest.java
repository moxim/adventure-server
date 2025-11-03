package com.pdg.adventure.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.pdg.adventure.AdventureBuilderServer;

@SpringBootTest
class ApplicationTest {

    @Test
    @Disabled("Disabled until we can run the server in a test environment")
    void checkSpringSetup() throws Exception {
        // given
        AdventureBuilderServer.main(new String[]{"", ""});

        // when

        // then
        Assertions.assertTrue(true); // If we reach this point, the context has loaded successfully
    }

}
