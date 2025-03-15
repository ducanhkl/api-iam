package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Schema(description = "Request parameters for listing permissions with pagination")
public record IndexPermissionRequestParamsDto(
        @Schema(description = "Filter permissions by name", example = "read:users")
        String permissionName,

        @Min(0)
        @Schema(description = "Page number for pagination (zero-based)", example = "0", defaultValue = "0")
        int page,

        @Min(1)
        @Max(100)
        @Schema(description = "Number of items per page", example = "20", defaultValue = "20")
        int size
) {}