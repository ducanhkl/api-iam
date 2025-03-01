package org.ducanh.apiiam.dto.requests;

public record UpdatePasswordRequestDto(String oldPassword,
                                       String newPassword,
                                       Boolean isLogoutOtherSession) {
}
