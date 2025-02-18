package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

import java.time.OffsetDateTime;

public record VerifyUserGroupResponseDto(
        String groupId,
        OffsetDateTime assignedAt
) {
}
