package com.example.todo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoRequest(
        @NotBlank(message = "제목은 비어 있거나 공백일 수 없습니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title
) {
}
