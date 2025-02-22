package org.ducanh.apiiam.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_permission_id", nullable = false, updatable = false)
    private long rolePermissionId;

    @Column(name = "role_id", nullable = false)
    private String roleId;

    @Column(name = "permission_id", nullable = false)
    private String permissionId;

    @Column(name = "namespace_id", nullable = false)
    private String namespaceId;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime assignedAt;
}
