package com.scaler.productservicejan31capstone.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class ImageGenerationAIService {

    @Value("${spring.ai.azure.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.azure.openai.endpoint}")
    private String endpoint;

    @Value("${ai.azure.openai.api.version}")
    private String apiVersion;

    public String generateImageUrl(String prompt) {
        String dalleDeployment = "dall-e-3";

        WebClient webClient = WebClient.builder()
                .baseUrl(endpoint)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", apiKey)
                .build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "dall-e-3");
        requestBody.put("prompt", prompt);
        requestBody.put("size", "1024x1024");
        requestBody.put("style", "vivid");
        requestBody.put("quality", "standard");
        requestBody.put("n", 1);

        String response = webClient.post()
                .uri("/openai/deployments/{deploymentId}/images/generations?api-version={version}", dalleDeployment, apiVersion)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Extract URL from JSON (could use Jackson instead for cleaner code)
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            return jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

//    private Mono<String> pollForResult(String operationUrl) {
//        return Mono.defer(() -> webClient.get()
//                .uri(operationUrl)
//                .header("api-key", apiKey)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .flatMap(response -> {
//                    String status = (String)response.get("status");
//                    if("succeeded".equalsIgnoreCase(status)) {
//                        List<Map<String, String>> data = (List<Map<String, String>>) ((Map<String, Object>) response.get("result")).get("data");
//                        return Mono.just(data.get(0).get("url"));
//                    } else if("failed".equalsIgnoreCase(status)) {
//                        return Mono.error(new RuntimeException("Image generation failed"));
//                    } else {
//                        return Mono.delay(Duration.ofSeconds(2)).then(pollForResult(operationUrl));  // retry after delay
//                    }
//                })
//        );
//
//    }

}
