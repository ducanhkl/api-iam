package org.ducanh.apiiam.entities;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;


@Entity
@Table(name = "groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Group {

    @Id
    @Column(name = "group_id", nullable = false, updatable = false)
    private String groupId;

    @Column(name = "group_name", nullable = false, unique = true, length = 100)
    private String groupName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "namespace_id", columnDefinition = "TEXT")
    private String namespaceId;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public GroupResponseDto toGroupResponseDto() {
        return GroupResponseDto.builder()
                .groupId(this.groupId)
                .groupName(this.groupName)
                .description(this.description)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
