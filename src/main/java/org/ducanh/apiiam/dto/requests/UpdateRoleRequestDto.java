package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequestDto(@NotBlank(message = "Role name is required")
                                   @Size(max = 100, message = "Role name must not exceed 100 characters")
                                   String roleName,
                                   String description,
                                   @NotNull(message = "Namespace ID is required")
                                   Long namespaceId) {
}
