package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record NamespaceResponseDto(
        String namespaceId,
        String namespaceName,
        String description,
        Long keyPairId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}