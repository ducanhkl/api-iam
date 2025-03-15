
package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login request")
public record UserLoginRequestDto(
        @Schema(description = "Username for authentication", example = "john.doe", required = true)
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "User password", example = "password123", required = true)
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "Namespace ID for the login context", example = "ns-12345", required = true)
        @NotBlank(message = "Namespace ID is required")
        String namespaceId) {
}
