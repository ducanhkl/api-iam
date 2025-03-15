package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Request DTO for assigning permissions to a role")
public record AssignPermissionsToRoleRequestDto(
        @Schema(
                description = "List of permission IDs to be assigned to the role",
                example = "[\"permission1\", \"permission2\", \"permission3\"]",
                minLength = 1,
                maxLength = 100
        )
        @NotEmpty(message = "Permission IDs list cannot be empty")
        @Size(max = 100, message = "Cannot assign more than 100 permissions at once")
        List<String> permissionIds
) {}