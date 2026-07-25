package com.synora.services;

import com.synora.dto.AiSummaryResponse;

import java.time.Instant;

public interface AiService {
    String fixText(String text);
    AiSummaryResponse summarizeRoom(Long userId, String roomId, Instant since);
}
