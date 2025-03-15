package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequestDto(
        @Schema(description = "Name of the permission", example = "READ_USER_DATA", required = true)
        @NotBlank(message = "Permission name is required")
        @Size(max = 100, message = "Permission name must not exceed 100 characters")
        String permissionName,

        @Schema(description = "Description of the permission", example = "Allows reading user profile data")
        String description) {
}
