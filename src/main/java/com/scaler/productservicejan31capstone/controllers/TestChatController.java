package com.scaler.productservicejan31capstone.controllers;

import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestChatController
{
    ChatClient chatClient;
    private final AzureOpenAiChatModel azureOpenAiChatModel;

    public TestChatController(ChatClient chatClient, AzureOpenAiChatModel azureOpenAiChatModel)
    {
        this.chatClient = chatClient;
        this.azureOpenAiChatModel = azureOpenAiChatModel;
    }

    @GetMapping("/chat")
    public List<Generation> chat()
    {
//        return chatClient.prompt().user(message).call().content();

        Prompt prompt = new Prompt("Can you tell me top 5 moves in chess game.");
        ChatResponse chatResponse = azureOpenAiChatModel.call(prompt);
        return chatResponse.getResults();
    }

}
