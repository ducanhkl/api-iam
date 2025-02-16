package org.ducanh.apiiam.dto.requests;

public record IndexUserRequestParamsDto(
                                        String username,
                                        String email,
                                        Long userId,
                                        int page,
                                        int size) {
}
