package com.synora.services.impl;

import com.synora.dto.GeminiResponse;
import com.synora.services.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private RestClient geminiRestClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Override
    public String fixText(String text) {
        String prompt = "Correct the grammar, spelling, and tone of the following chat message. " +
                "Return ONLY the corrected message \u2014 no explanation, no quotes, no preamble:\n\n" + text;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        GeminiResponse response = geminiRestClient.post()
                .header("x-goog-api-key", apiKey)
                .body(requestBody)
                .retrieve()
                .body(GeminiResponse.class);

        return extractText(response);
    }

    private String extractText(GeminiResponse response) {
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new RuntimeException("Empty response from AI service");
        }
        return response.getCandidates().get(0).getContent().getParts().get(0).getText().trim();
    }
}
