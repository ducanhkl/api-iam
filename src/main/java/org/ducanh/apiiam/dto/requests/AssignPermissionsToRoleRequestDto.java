package org.ducanh.apiiam.dto.requests;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AssignPermissionsToRoleRequestDto(
        @NotEmpty(message = "Permission IDs list cannot be empty")
        @Size(max = 100, message = "Cannot assign more than 100 permissions at once")
        List<String> permissionIds
) {}
