package org.massage.parlor.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String message;
}
