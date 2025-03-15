package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Request DTO for creating a new role")
public record CreateRoleRequestDto(
        @Schema(description = "Unique identifier for the role", example = "admin-role-1")
        String roleId,

        @NotBlank(message = "Role name is required")
        @Size(max = 100, message = "Role name must not exceed 100 characters")
        @Schema(description = "Name of the role", example = "Administrator")
        String roleName,

        @Schema(description = "Description of the role's purpose and permissions", example = "Full system access with all permissions")
        String description) {
}