package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AssignRolesToGroupRequestDto(
        @NotEmpty(message = "Role IDs list cannot be empty")
        @Size(max = 100, message = "Cannot assign more than 100 roles at once")
        List<String> roleIds
) {}