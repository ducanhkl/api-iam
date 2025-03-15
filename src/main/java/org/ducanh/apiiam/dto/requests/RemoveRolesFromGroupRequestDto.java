package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request DTO for removing roles from a group")
public record RemoveRolesFromGroupRequestDto(
        @NotEmpty(message = "Role IDs list cannot be empty")
        @Schema(description = "List of role IDs to remove from the group", required = true, example = "[\"admin-role\", \"user-manager-role\"]")
        List<String> roleIds
) {}