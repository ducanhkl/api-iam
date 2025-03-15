
package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Request for updating user information")
public record UpdateUserRequestDto(
        @Schema(description = "Username for the account", example = "john.doe", required = true)
        @NotBlank(message = "Username is required")
        @Size(max = 50)
        String username,

        @Schema(description = "Email address", example = "john.doe@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email
        @Size(max = 100)
        String email,

        @Schema(description = "Phone number", example = "+1234567890")
        @Size(max = 15)
        String phoneNumber,

        @Schema(description = "Multi-factor authentication enabled status", example = "true")
        Boolean mfaEnabled,

        @Schema(description = "Account locked status", example = "false")
        Boolean accountLocked) {
}