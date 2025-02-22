package org.ducanh.apiiam.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.ducanh.apiiam.dto.requests.CreateRoleRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateRoleRequestDto;
import org.ducanh.apiiam.dto.responses.CreateRoleResponseDto;
import org.ducanh.apiiam.dto.responses.RoleResponseDto;
import org.ducanh.apiiam.dto.responses.UpdateRoleResponseDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;


@Entity
@Table(name = "ROLES")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Role {

    @Id
    @Column(name = "ROLE_ID", length = 100, unique = true)
    private String roleId;

    @Column(name = "ROLE_NAME", length = 100)
    private String roleName;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "NAMESPACE_ID")
    private String namespaceId;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public static Role from(String namespaceId, CreateRoleRequestDto requestDto) {
        return Role.builder()
                .roleName(requestDto.roleName())
                .description(requestDto.description())
                .namespaceId(namespaceId)
                .build();
    }

    public static Role from(UpdateRoleRequestDto requestDto) {
        return Role.builder()
                .roleName(requestDto.roleName())
                .description(requestDto.description())
                .namespaceId(requestDto.namespaceId())
                .build();
    }

    public CreateRoleResponseDto toCreateResponseDto() {
        return CreateRoleResponseDto.builder()
                .roleId(this.roleId)
                .roleName(this.roleName)
                .description(this.description)
                .namespaceId(this.namespaceId)
                .createdAt(this.createdAt)
                .build();
    }

    public RoleResponseDto toResponseDto() {
        return RoleResponseDto.builder()
                .roleId(this.roleId)
                .roleName(this.roleName)
                .description(this.description)
                .namespaceId(this.namespaceId)
                .createdAt(this.createdAt)
                .build();
    }

    public UpdateRoleResponseDto toUpdateResponseDto() {
        return UpdateRoleResponseDto.builder()
                .roleId(this.roleId)
                .roleName(this.roleName)
                .description(this.description)
                .namespaceId(this.namespaceId)
                .createdAt(this.createdAt)
                .build();
    }

    public void update(UpdateRoleRequestDto requestDto) {
        this.roleName = requestDto.roleName();
        this.description = requestDto.description();
        this.namespaceId = requestDto.namespaceId();
    }

}
