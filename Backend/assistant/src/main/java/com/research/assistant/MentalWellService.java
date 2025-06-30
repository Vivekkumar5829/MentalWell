package com.research.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class MentalWellService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public MentalWellService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String processContent(MentalWellRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return "⚠️ Message cannot be empty.";
        }

        // Build the prompt for AI Career Copilot
        String prompt = "You are an AI Career Copilot designed to guide underserved students. " +
                "Provide friendly, step-by-step career guidance in simple terms. " +
                "Here is the question:\n\n" + request.getMessage();

        // Build the Gemini-compatible request body
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            String response = webClient.post()
                    .uri(geminiApiUrl + geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractTextFromResponse(response);

        } catch (Exception e) {
            return "⚠️ Error contacting Gemini API: " + e.getMessage();
        }
    }

    private String extractTextFromResponse(String response) {
        try {
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if (firstCandidate.getContent() != null &&
                        firstCandidate.getContent().getParts() != null &&
                        !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return "⚠️ No valid content found in Gemini response.";
        } catch (Exception e) {
            return "⚠️ Error parsing Gemini response: " + e.getMessage();
        }
    }
}
