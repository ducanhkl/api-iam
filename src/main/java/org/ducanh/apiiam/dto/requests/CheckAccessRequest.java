package org.ducanh.apiiam.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckAccessRequest(@NotNull List<@NotEmpty @NotNull String> groupId,
                                 @NotNull @NotEmpty String permissionId) {
}
