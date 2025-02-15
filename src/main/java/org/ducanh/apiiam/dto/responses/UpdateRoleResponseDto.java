package org.ducanh.apiiam.dto.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleResponseDto {
    private String roleId;
    private String roleName;
    private String description;
    private Long namespaceId;
    private OffsetDateTime createdAt;
}