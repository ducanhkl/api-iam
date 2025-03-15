
package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequestDto(
        @Schema(description = "Name of the role", example = "ADMIN", required = true)
        @NotBlank(message = "Role name is required")
        @Size(max = 100, message = "Role name must not exceed 100 characters")
        String roleName,

        @Schema(description = "Description of the role", example = "Administrative role with full access")
        String description,

        @Schema(description = "ID of the namespace this role belongs to", example = "ns-12345", required = true)
        @NotNull(message = "Namespace ID is required")
        String namespaceId) {
}