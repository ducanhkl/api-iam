package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO for creating a new namespace")
public record CreateNamespaceRequestDto(
        @Schema(
                description = "Unique identifier for the namespace",
                example = "my-namespace-123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Namespace ID is required")
        String namespaceId,

        @Schema(
                description = "Name of the namespace",
                example = "My Application Namespace",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Namespace name is required")
        @Size(max = 100, message = "Namespace name must not exceed 100 characters")
        String namespaceName,

        @Schema(
                description = "Description of the namespace's purpose",
                example = "Namespace for my application's resources",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String description,

        @Schema(
                description = "ID of the key pair associated with this namespace",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Key pair ID is required")
        Long keyPairId
) {}