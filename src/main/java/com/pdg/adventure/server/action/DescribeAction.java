package com.pdg.adventure.server.action;

// DISABLED: Spring AI not compatible with Spring Boot 4.0.1 yet
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.ollama.api.OllamaApi;
//import org.springframework.ai.ollama.api.OllamaModel;
//import org.springframework.ai.ollama.api.OllamaOptions;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.web.client.RestClient;
//import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Supplier;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

public class DescribeAction extends AbstractAction {

    private final transient Supplier<String> target;

    public DescribeAction(Supplier<String> aFunction, MessagesHolder aMessagesHolder) {
        super(aMessagesHolder);
        target = aFunction;
    }

    @Override
    public ExecutionResult execute() {
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);

//        final String response = fillThroughAI(target.get());
//        result.setResultMessage(response);
        result.setResultMessage(target.get());
        return result;
    }

    // DISABLED: Spring AI not compatible with Spring Boot 4.0.1 yet
    // Will be re-enabled when Spring AI 2.0.0 is released
//    private String fillThroughAI(String aRequest) {
//        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//        requestFactory.setConnectTimeout(10_000); // 10 seconds for connection
//        requestFactory.setReadTimeout(60_000);    // 60 seconds for reading response
//
//        RestClient.Builder restClient = RestClient.builder()
//                                                  .requestFactory(requestFactory);
//
//        OllamaApi ollamaApi = new OllamaApi("http://www.pdg-software.com:11434", restClient, WebClient.builder());
//
//        OllamaChatModel chatModel = OllamaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(
//                OllamaOptions.builder().model(OllamaModel.LLAMA3_2_1B)
//                             .temperature(0.5) // Less randomness
//                             .build()).build();
//
//        ChatClient chatClient = ChatClient.builder(chatModel).build();
//
//        System.out.println("Request: " + aRequest);
//
//        ChatClient.ChatClientRequestSpec chatResponse =
//                chatClient.prompt()
//                          .system("""
//                                          You are an author of fantasy novels like the lord of the rings. You will now be presented with
//                                          a short description of a location in a fantasy world along with obvious exits.
//                                          Please elaborate on the following so that it sounds like a short excerpt of you novels,
//                                          but only use 100 words and do mention the exits.
//                                          """)
//                          .user(aRequest);
//
//        String response = chatResponse.call().content();
//        return response;
//    }
}
