package org.ducanh.apiiam.dto.responses;

public record TokenRefreshResponse(String accessToken, String refreshToken) {
}
