package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RemoveRolesFromGroupRequestDto(
        @NotEmpty(message = "Role IDs list cannot be empty")
        List<String> roleIds
) {}