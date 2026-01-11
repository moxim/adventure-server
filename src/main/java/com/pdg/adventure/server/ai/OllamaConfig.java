package com.pdg.adventure.server.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO: make use of this properly in the DescribeAction class
@Configuration
public class OllamaConfig {
    @Bean
    OllamaChatModel ollamaChatModel() {
        var ollamaApi = OllamaApi.builder().build();

        return OllamaChatModel.builder()
                              .ollamaApi(ollamaApi)
                              .defaultOptions(
                                      OllamaChatOptions.builder()
                                                       .model(OllamaModel.LLAMA3_2_1B)
                                                       .temperature(0.9)
                                                       .build())
                              .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(
                              """
                                      You are an author of fantasy novels like the lord of the rings. You will now be
                                      presented with a short description of a location in a fantasy world along with
                                      obvious exits. Please elaborate on the following so that it sounds like a short
                                      excerpt of your novels, but only use 100 words and do mention the exits.
                                      """
                      )
                      .build();
    }

}
