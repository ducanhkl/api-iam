package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request DTO for checking access permissions")
public record CheckAccessRequest(
        @Schema(
                description = "List of group IDs to check access for",
                example = "[\"group1\", \"group2\", \"group3\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        List<@NotEmpty @NotNull String> groupId,

        @Schema(
                description = "Permission ID to check access against",
                example = "permission1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @NotEmpty
        String permissionId
) {}