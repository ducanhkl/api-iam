package org.ducanh.apiiam.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.AssignRolesToGroupRequestDto;
import org.ducanh.apiiam.dto.requests.RemoveRolesFromGroupRequestDto;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.dto.responses.GroupRoleResponseDto;
import org.ducanh.apiiam.services.GroupRoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

import static org.ducanh.apiiam.Constants.*;

@RestController
@RequestMapping("/group-role")
@Slf4j
@RequiredArgsConstructor
public class GroupRoleController {

    private final GroupRoleService groupRoleService;

    @PostMapping("/groups/{groupId}/roles")
    public ResponseEntity<Void> assignRolesToGroup(
            @PathVariable String groupId,
            @Valid @RequestBody AssignRolesToGroupRequestDto request
    ) {
        log.info("Assigning roles {} to group {}", request.roleIds(), groupId);
        groupRoleService.assignRolesToGroup(groupId, request.roleIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/groups/{groupId}/roles")
    public ResponseEntity<Void> removeRolesFromGroup(
            @PathVariable String groupId,
            @Valid @RequestBody RemoveRolesFromGroupRequestDto request
    ) {
        log.info("Removing roles {} from group {}", request.roleIds(), groupId);
        groupRoleService.removeRolesFromGroup(groupId, request.roleIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/groups/{groupId}/roles")
    public ResponseEntity<List<GroupRoleResponseDto>> getGroupRoles(
            @PathVariable String groupId,
            @RequestParam(required = false) String roleName,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<GroupRoleResponseDto> result = groupRoleService.getGroupRoles(groupId, roleName, pageable);
        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(result.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(result.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }

    @GetMapping("/roles/{roleId}/groups")
    public ResponseEntity<List<GroupResponseDto>> getRoleGroups(
            @PathVariable String roleId,
            @RequestParam(required = false) String groupName,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<GroupResponseDto> result = groupRoleService.getRoleGroups(roleId, groupName, pageable);
        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(result.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(result.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }
}
