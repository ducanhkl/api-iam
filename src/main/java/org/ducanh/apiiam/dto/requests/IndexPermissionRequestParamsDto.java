package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

public record IndexPermissionRequestParamsDto(
        String permissionName,
        @Min(0)
        int page,
        @Min(1)
        @Max(100)
        int size
) {}