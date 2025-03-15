package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request for verifying multiple groups")
public record VerifyGroupsRequestDto(
        @Schema(description = "List of group IDs to verify", example = "[\"group-123\", \"group-456\"]", required = true)
        @NotEmpty(message = "Group IDs list cannot be empty")
        @Size(max = 100, message = "Cannot verify more than 100 groups at once")
        List<String> groupIds
) {}