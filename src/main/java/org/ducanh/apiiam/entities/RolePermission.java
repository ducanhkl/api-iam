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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_permission_id_seq")
    @SequenceGenerator(
            name = "role_permission_id_seq",
            sequenceName = "role_permission_id_seq",
            allocationSize = 100
    )
    @Column(name = "role_permission_id")
    private long rolePermissionId;

    @Column(name = "role_id")
    private String roleId;

    @Column(name = "permission_id")
    private String permissionId;

    @Column(name = "namespace_id")
    private String namespaceId;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime assignedAt;
}
