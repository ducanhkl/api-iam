package org.ducanh.apiiam.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.ducanh.apiiam.dto.responses.PermissionResponseDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Permission {
    @Id
    @Column(name = "permission_id", nullable = false, updatable = false)
    private String permissionId;

    @Column(name = "permission_name", nullable = false, unique = true, length = 100)
    private String permissionName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "namespaceId", columnDefinition = "TEXT")
    private String namespaceId;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public PermissionResponseDto toPermissionResponseDto() {
        return PermissionResponseDto.builder()
                .permissionId(this.permissionId)
                .permissionName(this.permissionName)
                .description(this.description)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
