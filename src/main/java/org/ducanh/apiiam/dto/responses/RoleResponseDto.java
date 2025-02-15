package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record RoleResponseDto(String roleId,
                              String roleName,
                              String description,
                              Long namespaceId,
                              OffsetDateTime createdAt) {
}
