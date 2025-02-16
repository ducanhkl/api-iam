package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequestDto(@NotBlank(message = "Permission ID is required")
                                         String permissionId,
                                         @NotBlank(message = "Permission name is required")
                                         @Size(max = 100, message = "Permission name must not exceed 100 characters")
                                         String permissionName,
                                         String namespaceId,
                                         String description) {
}
