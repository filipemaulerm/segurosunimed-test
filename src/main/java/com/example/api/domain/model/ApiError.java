package com.example.api.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@RequiredArgsConstructor
public class ApiError {
    private final String message;
}