package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateUserRequestDto(@NotBlank(message = "Username is required")
                                   @Size(max = 50)
                                   String username,

                                   @NotBlank(message = "Email is required")
                                   @Email
                                   @Size(max = 100)
                                   String email,

                                   @Size(max = 15)
                                   String phoneNumber,

                                   Boolean mfaEnabled,
                                   Boolean accountLocked) {
}
