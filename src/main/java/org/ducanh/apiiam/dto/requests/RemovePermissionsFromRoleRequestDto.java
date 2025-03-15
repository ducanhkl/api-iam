package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request DTO for removing permissions from a role")
public record RemovePermissionsFromRoleRequestDto(
        @NotEmpty(message = "Permission IDs list cannot be empty")
        @Schema(description = "List of permission IDs to remove from the role", required = true, example = "[\"read:users\", \"write:users\"]")
        List<String> permissionIds
) {}