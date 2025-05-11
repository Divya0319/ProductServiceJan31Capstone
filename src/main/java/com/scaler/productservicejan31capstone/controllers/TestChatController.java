package com.scaler.productservicejan31capstone.controllers;

import com.scaler.productservicejan31capstone.dtos.ChatRequest;
import com.scaler.productservicejan31capstone.services.ImageGenerationAIService;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class TestChatController
{
    ChatClient chatClient;
    private final AzureOpenAiChatModel azureOpenAiChatModel;

    private final ImageGenerationAIService imageGenerationAIService;

    public TestChatController(ChatClient chatClient, AzureOpenAiChatModel azureOpenAiChatModel, ImageGenerationAIService imageGenerationAIService)
    {
        this.chatClient = chatClient;
        this.azureOpenAiChatModel = azureOpenAiChatModel;
        this.imageGenerationAIService = imageGenerationAIService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request)
    {
//        return chatClient.prompt().user(message).call().content();

        Prompt prompt = new Prompt(request.getPrompt());
        ChatResponse chatResponse = azureOpenAiChatModel.call(prompt);
        return chatResponse.getResults().get(0).getOutput().getText();
    }

    @PostMapping("/generate-image")
    public ResponseEntity<String> generateImage(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        String imageUrl = imageGenerationAIService.generateImageUrl(prompt);
        return ResponseEntity.ok(imageUrl); // ✅ returns 200 with body
    }

}
