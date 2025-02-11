package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

@Builder
public record UserLoginResponseDto(
        String refreshToken,
        String accessToken
) {
}
