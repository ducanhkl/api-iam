package org.ducanh.apiiam.dto.responses;

import lombok.Builder;
import org.ducanh.apiiam.entities.UserStatus;

import java.time.OffsetDateTime;

@Builder
public record CreateUserResponseDto(Long userId,
                                    String username,
                                    String email,
                                    Boolean isVerified,
                                    String namespaceId,
                                    UserStatus status,
                                    OffsetDateTime lastLogin,
                                    Boolean mfaEnabled,
                                    Boolean accountLocked,
                                    String phoneNumber,
                                    OffsetDateTime createdAt,
                                    OffsetDateTime updatedAt) {
}
