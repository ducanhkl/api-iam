package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

@Builder
public record UserRegisterResponseDto(String username, Long userId, String namespaceId) {
}
