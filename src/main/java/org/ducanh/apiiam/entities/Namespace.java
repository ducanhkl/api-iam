package org.ducanh.apiiam.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.ducanh.apiiam.dto.responses.NamespaceResponseDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "namespace")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class Namespace {

    @Id
    @Column(name = "namespace_id", nullable = false, updatable = false)
    private String namespaceId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String namespaceName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "key_pair_id")
    private Long keyPairId;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public NamespaceResponseDto toNamespaceResponseDto() {
        return NamespaceResponseDto.builder()
                .namespaceId(this.namespaceId)
                .namespaceName(this.namespaceName)
                .description(this.description)
                .keyPairId(this.keyPairId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}