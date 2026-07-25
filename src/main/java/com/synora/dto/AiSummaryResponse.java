package com.synora.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiSummaryResponse {
    private String summary;
    private long messageCount;
}