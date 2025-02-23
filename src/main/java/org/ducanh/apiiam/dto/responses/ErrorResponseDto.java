package org.ducanh.apiiam.dto.responses;

import lombok.Builder;

@Builder
public record ErrorResponseDto(
        String errorCode,
        String shortDescriptions,
        String longDescription,
        String appName,
        String appVersion
) {
}
