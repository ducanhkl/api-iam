package org.ducanh.apiiam.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.AssignGroupsForUserRequestDto;
import org.ducanh.apiiam.dto.requests.RemoveUserFromGroupsRequestDto;
import org.ducanh.apiiam.dto.requests.VerifyGroupsRequestDto;
import org.ducanh.apiiam.dto.responses.UserGroupResponseDto;
import org.ducanh.apiiam.dto.responses.VerifyUserGroupResponseDto;
import org.ducanh.apiiam.services.UserGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ducanh.apiiam.Constants.*;
import static org.ducanh.apiiam.Constants.TOTAL_PAGES_HEADER;

@RestController
@RequestMapping("/user-group/")
@Slf4j
@RequiredArgsConstructor
public class UserGroupController {

    private final UserGroupService userGroupService;

    @PostMapping("user-id/{userId}/groups")
    public ResponseEntity<Void> assignGroupsToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignGroupsForUserRequestDto request) {
        log.info("Assigning groups {} to user {}", request.groupIds(), userId);
        userGroupService.assignGroupsToUser(userId, request.groupIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("user-id/{userId}/groups")
    public ResponseEntity<List<UserGroupResponseDto>> getUserGroups(
            @PathVariable Long userId,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false, defaultValue = "false") Boolean assignedOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting groups for user {} with filter: {}", userId, groupName);
        Page<UserGroupResponseDto> result = userGroupService.getUserGroups(userId, groupName, assignedOnly, pageable);
        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(result.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(result.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }

    @PostMapping("user-id/{userId}/groups/verify")
    public ResponseEntity<List<VerifyUserGroupResponseDto>> verifyUserGroups(
            @PathVariable Long userId,
            @Valid @RequestBody VerifyGroupsRequestDto request) {
        log.info("Verifying groups for user {}", userId);
        return ResponseEntity.ok(userGroupService.verifyUserGroups(userId, request.groupIds()));
    }

    @DeleteMapping("user-id/{userId}/groups/")
    public ResponseEntity<Void> deleteUserGroups(
            @PathVariable Long userId,
            @Valid @RequestBody RemoveUserFromGroupsRequestDto request
            ) {
        userGroupService.removeUserFromGroups(userId, request.groupIds());
        return ResponseEntity.ok().build();
    }
}
