package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request DTO for removing a user from multiple groups")
public record RemoveUserFromGroupsRequestDto(
        @Schema(description = "List of group IDs from which to remove the user", example = "[\"admin-group\", \"finance-group\"]")
        List<String> groupIds
) {
}