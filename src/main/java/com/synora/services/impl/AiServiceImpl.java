package com.synora.services.impl;

import com.synora.dto.AiSummaryResponse;
import com.synora.dto.GeminiResponse;
import com.synora.entities.Message;
import com.synora.entities.RoomMembership;
import com.synora.repositories.MessageRepository;
import com.synora.repositories.RoomMembershipRepository;
import com.synora.services.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private RestClient geminiRestClient;
    @Autowired
    private RoomMembershipRepository membershipRepository;
    @Autowired
    private MessageRepository messageRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Override
    public String fixText(String text) {
        String prompt = "Correct the grammar, spelling, and tone of the following chat message. " +
                "Return ONLY the corrected message \u2014 no explanation, no quotes, no preamble:\n\n" + text;
        return callGemini(prompt);
    }

    @Override
    public AiSummaryResponse summarizeRoom(Long userId, String roomId, Instant since) {
        RoomMembership membership = membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .orElseThrow(() -> new RuntimeException("Not a member of this room"));

        Instant cutoff = since != null ? since : membership.getLastReadAt();

        List<Message> unread = messageRepository
                .findByRoomIdAndTimestampAfterOrderByTimestampAsc(roomId, cutoff);

        if (unread.isEmpty()) {
            return new AiSummaryResponse("You're all caught up \u2014 no new messages.", 0);
        }

        String transcript = unread.stream()
                .map(m -> m.getSender() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        String prompt = "Summarize the following unread chat messages in 2-3 short sentences, " +
                "focused on what a returning team member needs to know. Write a natural summary, " +
                "don't just restate the raw messages:\n\n" + transcript;

        return new AiSummaryResponse(callGemini(prompt), unread.size());
    }

    private String callGemini(String prompt) {
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
