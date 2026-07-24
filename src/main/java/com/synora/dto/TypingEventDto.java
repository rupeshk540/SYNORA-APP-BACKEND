package com.synora.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypingEventDto {

    private String sender;
    private boolean typing;
}
