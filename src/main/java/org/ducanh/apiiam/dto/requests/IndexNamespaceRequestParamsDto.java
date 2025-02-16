package org.ducanh.apiiam.dto.requests;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record IndexNamespaceRequestParamsDto(
        String namespaceName,
        @Min(0)
        int page,
        @Min(1)
        @Max(100)
        int size
) {}