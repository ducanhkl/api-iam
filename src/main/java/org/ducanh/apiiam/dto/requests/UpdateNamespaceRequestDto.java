package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request DTO for updating a namespace")
public record UpdateNamespaceRequestDto(
        @Schema(description = "New name for the namespace", example = "production-environment")
        String namespaceName,

        @Schema(description = "New description for the namespace", example = "Production environment resources and configurations")
        String description,

        @Schema(description = "ID of the key pair associated with this namespace", example = "42")
        Long keyPairId
) {}