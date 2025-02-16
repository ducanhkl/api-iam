package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequestDto(@NotBlank(message = "Permission name is required")
                                         @Size(max = 100, message = "Permission name must not exceed 100 characters")
                                         String permissionName,
                                         String description) {
}
