package com.example.chatroomserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${ai.hf.token}")
    private String token;

    @Value("${ai.hf.model}")
    private String model;

    @Value("${ai.hf.endpoint}")
    private String endpoint;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public String suggestReply(String lastMessage) {

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "Suggest a short, natural chat reply."),
                        Map.of("role", "user", "content", lastMessage)
                )
        );

        try {
            String body = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());
            return json.at("/choices/0/message/content").asText();

        } catch (Exception e) {
            throw new RuntimeException("AI request failed", e);
        }
    }
}
