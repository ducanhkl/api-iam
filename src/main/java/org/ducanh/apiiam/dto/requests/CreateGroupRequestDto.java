package org.ducanh.apiiam.dto.requests;

import lombok.Builder;

@Builder
public record CreateGroupRequestDto(String groupId,
                                    String groupName,
                                    String description) {
}
