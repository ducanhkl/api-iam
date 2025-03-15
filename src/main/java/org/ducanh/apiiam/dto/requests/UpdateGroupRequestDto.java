package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request DTO for updating a group")
public record UpdateGroupRequestDto(
        @Schema(description = "New name for the group", example = "Finance Department")
        String groupName,

        @Schema(description = "New description for the group", example = "Users with access to financial systems and reports")
        String description) {
}