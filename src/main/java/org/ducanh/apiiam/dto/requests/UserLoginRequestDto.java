package org.ducanh.apiiam.dto.requests;

public record UserLoginRequestDto(String username, String password, String namespaceId) {
}
