package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

@Builder
public record RolePermissionResponseDto(
        String permissionId,
        String permissionName,
        String description
) {}