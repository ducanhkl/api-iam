package org.ducanh.apiiam.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.AssignGroupsRequestDto;
import org.ducanh.apiiam.dto.requests.VerifyGroupsRequestDto;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.dto.responses.VerifyUserGroupResponseDto;
import org.ducanh.apiiam.services.UserGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-group/")
@Slf4j
@RequiredArgsConstructor
public class UserGroupController {

    private final UserGroupService userGroupService;

    @PostMapping("users/{userId}/groups")
    public ResponseEntity<Void> assignGroupsToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignGroupsRequestDto request) {
        log.info("Assigning groups {} to user {}", request.groupIds(), userId);
        userGroupService.assignGroupsToUser(userId, request.groupIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<GroupResponseDto>> getUserGroups(
            @PathVariable Long userId,
            @RequestParam(required = false) String groupName,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting groups for user {} with filter: {}", userId, groupName);
        return ResponseEntity.ok(userGroupService.getUserGroups(userId, groupName, pageable));
    }

    @GetMapping("/not-belong")
    public ResponseEntity<Page<GroupResponseDto>> getGroupsNotBelongToUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String groupName,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting groups not belonging to user {} with filter: {}", userId, groupName);
        return ResponseEntity.ok(userGroupService.getUserGroupsNotBelongToUser(userId, groupName, pageable));
    }

    @PostMapping("/verify")
    public ResponseEntity<List<VerifyUserGroupResponseDto>> verifyUserGroups(
            @PathVariable Long userId,
            @Valid @RequestBody VerifyGroupsRequestDto request) {
        log.info("Verifying groups for user {}", userId);
        return ResponseEntity.ok(userGroupService.verifyUserGroups(userId, request.groupIds()));
    }
}
