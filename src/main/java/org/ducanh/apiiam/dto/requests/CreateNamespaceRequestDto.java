package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNamespaceRequestDto(
        @NotBlank(message = "Namespace ID is required")
        String namespaceId,

        @NotBlank(message = "Namespace name is required")
        @Size(max = 100, message = "Namespace name must not exceed 100 characters")
        String namespaceName,

        String description,

        @NotNull(message = "Key pair ID is required")
        Long keyPairId
) {}