package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.ducanh.apiiam.entities.UserStatus;

@Builder
public record CreateUserRequestDto(@NotBlank(message = "Username is required")
                                   @Size(max = 50)
                                   String username,
                                   @NotBlank(message = "Email is required")
                                   @Email
                                   @Size(max = 100)
                                   String email,
                                   @NotBlank(message = "Password is required")
                                   String password,
                                   @Size(max = 15)
                                   String phoneNumber,
                                   @NotNull(message = "Namespace ID is required")
                                   String namespaceId,
                                   @NotNull(message = "Should the user be verified immediately")
                                   Boolean isVerified,
                                   Boolean mfaEnabled,
                                   UserStatus status) {
}
