package org.ducanh.apiiam.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_group_id_seq")
    @SequenceGenerator(
            name = "user_group_id_seq",
            sequenceName = "user_group_id_seq",
            allocationSize = 100
    )
    @Column(name = "user_group_id", unique = true)
    private Long userGroupId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "group_id", length = 50)
    private String groupId;

    @Column(name = "namespace_id", length = 50)
    private String namespaceId;

    @Column(name = "assigned_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime assignedAt;
}
