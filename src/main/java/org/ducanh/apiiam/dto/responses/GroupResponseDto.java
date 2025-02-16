package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record GroupResponseDto(
        String groupId,
        String groupName,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
