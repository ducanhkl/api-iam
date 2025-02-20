package org.ducanh.apiiam.dto.responses;

import lombok.Builder;
import java.time.OffsetDateTime;

@Builder
public record GroupRoleResponseDto(
        String roleId,
        String roleName,
        String description,
        OffsetDateTime assignedAt
) {}