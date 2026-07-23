package com.synora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadReceiptUpdate {
    private String roomId;
    private String readBy;
    private Instant readUpTo;
}