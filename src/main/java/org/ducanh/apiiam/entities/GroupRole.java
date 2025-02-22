package org.ducanh.apiiam.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;

@Entity
@Table(name = "group_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class GroupRole {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_role_id_seq")
    @SequenceGenerator(
            name = "group_role_id_seq",
            sequenceName = "group_role_id_seq",
            allocationSize = 100
    )
    @Column(name = "group_role_id")
    private Long groupRoleId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "role_id")
    private String roleId;

    @Column(name = "namespace_id")
    private String namespaceId;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime assignedAt;
}
