package org.ducanh.apiiam.dto.requests;

import java.util.List;

public record RemoveUserFromGroupsRequestDto(
        List<String> groupIds
) {
}
