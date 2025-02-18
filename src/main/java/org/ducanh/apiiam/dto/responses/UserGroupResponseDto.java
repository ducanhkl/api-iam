package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record UserGroupResponseDto(String groupId,
                                   String groupName,
                                   String description,
                                   OffsetDateTime createdAt,
                                   Boolean assigned,
                                   OffsetDateTime updatedAt) {
}
