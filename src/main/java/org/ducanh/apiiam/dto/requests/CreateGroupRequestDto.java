package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Request DTO for creating a new group")
@Builder
public record CreateGroupRequestDto(
        @Schema(
                description = "Unique identifier for the group",
                example = "admin-group",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String groupId,

        @Schema(
                description = "Name of the group",
                example = "Administrators",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String groupName,

        @Schema(
                description = "Description of the group's purpose",
                example = "Group for system administrators",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String description
) {}