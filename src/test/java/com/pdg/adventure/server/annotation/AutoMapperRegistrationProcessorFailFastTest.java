package com.pdg.adventure.server.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class AutoMapperRegistrationProcessorFailFastTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Test
    void afterSingletonsInstantiated_throws_insteadOfSwallowing_whenRegistrationFails() {
        AutoMapperRegistrationProcessor processor = new AutoMapperRegistrationProcessor(mapperSupporter);
        processor.postProcessAfterInitialization(new FakeMapper(), "fakeMapper");

        doThrow(new RuntimeException("boom")).when(mapperSupporter).registerMapper(any(), any(), any());

        assertThatThrownBy(processor::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fakeMapper")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @AutoRegisterMapper(description = "fake mapper for fail-fast testing")
    static class FakeMapper implements Mapper<String, Integer> {
        @Override
        public Integer mapToBO(String from) {
            return 0;
        }

        @Override
        public String mapToDO(Integer from) {
            return "";
        }
    }
}
