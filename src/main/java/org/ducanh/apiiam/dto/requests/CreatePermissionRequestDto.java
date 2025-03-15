package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO for creating a new permission")
public record CreatePermissionRequestDto(
        @Schema(
                description = "Unique identifier for the permission",
                example = "permission_read",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Permission ID is required")
        String permissionId,

        @Schema(
                description = "Name of the permission",
                example = "Read Permission",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Permission name is required")
        @Size(max = 100, message = "Permission name must not exceed 100 characters")
        String permissionName,

        @Schema(
                description = "Description of the permission",
                example = "Allows reading of resources",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String description
) {}