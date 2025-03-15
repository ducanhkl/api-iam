package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.ducanh.apiiam.entities.UserStatus;

@Builder
@Schema(description = "Request DTO for creating a new user")
public record CreateUserRequestDto(
        @NotBlank(message = "Username is required")
        @Size(max = 50)
        @Schema(description = "Unique username for the user", example = "johndoe")
        String username,

        @NotBlank(message = "Email is required")
        @Email
        @Size(max = 100)
        @Schema(description = "Email address of the user", example = "john.doe@example.com")
        String email,

        @NotBlank(message = "Password is required")
        @Schema(description = "User's password", example = "StrongP@ssw0rd")
        String password,

        @Size(max = 15)
        @Schema(description = "User's phone number", example = "+1234567890")
        String phoneNumber,

        @NotNull(message = "Namespace ID is required")
        @Schema(description = "ID of the namespace the user belongs to",  example = "default-namespace")
        String namespaceId,

        @NotNull(message = "Should the user be verified immediately")
        @Schema(description = "Flag indicating if the user should be verified immediately",  example = "true")
        Boolean isVerified,

        @Schema(description = "Flag indicating if multi-factor authentication is enabled", example = "false")
        Boolean mfaEnabled,

        @Schema(description = "Status of the user account", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED", "PENDING"})
        UserStatus status) {
}