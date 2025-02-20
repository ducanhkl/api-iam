package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AssignGroupsForUserRequestDto(
        @NotEmpty(message = "Group IDs list cannot be empty")
        @Size(max = 100, message = "Cannot assign more than 100 groups at once")
        List<String> groupIds
) {}