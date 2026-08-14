package com.neighborparking.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiError {
    private final String code;
    private final String message;
    private final String path;
    private final Instant timestamp;
    private final Map<String, String> fieldErrors;
}
