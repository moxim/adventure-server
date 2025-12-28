package com.pdg.adventure.server.action;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;

import java.util.function.Supplier;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

// TODO: integrate properly with OllamaConfig and use dependency injection
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

    private String fillThroughAI(String aRequest) {
        var ollamaApi = OllamaApi.builder().baseUrl("http://www.pdg-software.com:11434").build();

        OllamaChatModel ollamaChatModel = OllamaChatModel.builder()
                                                         .ollamaApi(ollamaApi)
                                                         .defaultOptions(
                                                                 OllamaChatOptions.builder()
                                                                                  .model(OllamaModel.LLAMA3_2_1B)
                                                                                  .temperature(0.4)
                                                                                  .build())
                                                         .build();
        ChatResponse response = ollamaChatModel.call(
                new Prompt(aRequest).augmentSystemMessage(
                        """
                                You are an author of fantasy novels like the lord of the rings. You will now be
                                presented with a short description of a location in a fantasy world along with
                                obvious exits. Please elaborate on the following so that it sounds like a short
                                excerpt of your novels, but only use 100 words and do mention the exits.
                                """
                )
        );

        System.out.println("Request: " + aRequest);

        return response.getResult().getOutput().getText();
    }
}
