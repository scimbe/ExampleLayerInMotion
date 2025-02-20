package com.example.motion.api.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String error;
    private final String message;
}