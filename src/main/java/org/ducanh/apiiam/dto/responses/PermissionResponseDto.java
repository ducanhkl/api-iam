package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record PermissionResponseDto(String permissionId,
                                    String permissionName,
                                    String description,
                                    OffsetDateTime createdAt,
                                    OffsetDateTime updatedAt) {
}
