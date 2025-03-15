package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Request parameters for listing users with filtering and pagination")
public record IndexUserRequestParamsDto(
        @Schema(description = "Filter users by username", example = "johndoe")
        String username,

        @Schema(description = "Filter users by email", example = "john.doe@example.com")
        String email,

        @Schema(description = "Filter users by user ID", example = "12345")
        Long userId,

        @Schema(description = "Page number for pagination (zero-based)", example = "0", defaultValue = "0")
        @Min(0)
        int page,

        @Schema(description = "Number of items per page", example = "20", defaultValue = "20")
        @Min(1)
        int size) {
}