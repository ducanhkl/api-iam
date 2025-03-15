
package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request")
public record UserRegisterRequestDto(
        @Schema(description = "Username for the new account", example = "john.doe", required = true)
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username must not exceed 50 characters")
        String username,

        @Schema(description = "Password for the new account", example = "SecurePass123", required = true)
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "Email address", example = "john.doe@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Schema(description = "Phone number", example = "+1234567890")
        @Size(max = 15, message = "Phone number must not exceed 15 characters")
        String phoneNumber) {
}