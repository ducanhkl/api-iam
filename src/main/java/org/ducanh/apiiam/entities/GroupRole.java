package org.ducanh.apiiam.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;

@Entity
@Table(name = "group_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "role_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class GroupRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_role_id", nullable = false, updatable = false)
    private long groupRoleId;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "role_id", nullable = false)
    private String roleId;

    @Column(name = "namespace_id", nullable = false)
    private String namespaceId;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime assignedAt;
}
