package org.ducanh.apiiam.dto.requests;

public record UpdateNamespaceRequestDto(
        String namespaceName,
        String description,
        Long keyPairId
) {}