package com.example.todo.api.dto;

import jakarta.validation.constraints.NotNull;

public record CompletionRequest(
        @NotNull(message = "completed 값은 필수입니다.")
        Boolean completed
) {
}
