package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RemovePermissionsFromRoleRequestDto(
        @NotEmpty(message = "Permission IDs list cannot be empty")
        List<String> permissionIds
) {}
