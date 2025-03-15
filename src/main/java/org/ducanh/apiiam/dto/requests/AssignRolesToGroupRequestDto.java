package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Request DTO for assigning roles to a group")
public record AssignRolesToGroupRequestDto(
        @Schema(
                description = "List of role IDs to be assigned to the group",
                example = "[\"role1\", \"role2\", \"role3\"]",
                minLength = 1,
                maxLength = 100
        )
        @NotEmpty(message = "Role IDs list cannot be empty")
        @Size(max = 100, message = "Cannot assign more than 100 roles at once")
        List<String> roleIds
) {}