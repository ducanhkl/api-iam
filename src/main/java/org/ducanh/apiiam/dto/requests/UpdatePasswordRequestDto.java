package org.ducanh.apiiam.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequestDto(
        @Schema(description = "User's current password", example = "CurrentPass123", required = true)
        @NotBlank(message = "Old password is required")
        String oldPassword,

        @Schema(description = "User's new password", example = "NewSecurePass456", required = true)
        @NotBlank(message = "New password is required")
        String newPassword,

        @Schema(description = "Flag to logout from other sessions after password change", example = "true")
        Boolean isLogoutOtherSession) {
}