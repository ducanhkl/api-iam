package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request DTO for assigning groups to a user")
public record AssignGroupsForUserRequestDto(
        @Schema(
                description = "List of group IDs to be assigned to the user",
                example = "[\"group1\", \"group2\", \"group3\"]",
                minLength = 1,
                maxLength = 100
        )
        @NotEmpty(message = "Group IDs list cannot be empty")
        @Size(max = 100, message = "Cannot assign more than 100 groups at once")
        List<String> groupIds
) {}