package com.pdg.adventure.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest {

    @Test
    void checkSpringSetup() throws Exception {
        // given
        AdventureBuilderServer.main(new String[]{"", ""});

        // when

        // then
    }

}
