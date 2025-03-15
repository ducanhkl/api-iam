package org.ducanh.apiiam.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.AssignRolesToGroupRequestDto;
import org.ducanh.apiiam.dto.requests.RemoveRolesFromGroupRequestDto;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.dto.responses.GroupRoleResponseDto;
import org.ducanh.apiiam.services.GroupRoleService;
import org.ducanh.apiiam.services.NamespaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

import static org.ducanh.apiiam.Constants.*;

@RestController
@RequestMapping("/group-role/{namespaceId}")
@Tag(name = "Group Role Controller", description = "Operations for managing relationships between groups and roles")
@Slf4j
@RequiredArgsConstructor
public class GroupRoleController {

    private final GroupRoleService groupRoleService;
    private final NamespaceService namespaceService;

    @PostMapping("/group-id/{groupId}/roles")
    @Operation(summary = "Assign roles to group",
            description = "Assigns multiple roles to a specific group within a namespace")
    public ResponseEntity<Void> assignRolesToGroup(
            @PathVariable String groupId,
            @PathVariable String namespaceId,
            @Valid @RequestBody AssignRolesToGroupRequestDto request
    ) {
        log.info("Assigning roles {} to group {}", request.roleIds(), groupId);
        groupRoleService.assignRolesToGroup(namespaceId, groupId, request.roleIds());
        namespaceService.increaseNamespaceVersion(namespaceId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/group-id/{groupId}/roles")
    @Operation(summary = "Remove roles from group",
            description = "Removes specified roles from a group within a namespace")
    public ResponseEntity<Void> removeRolesFromGroup(
            @PathVariable String groupId,
            @PathVariable String namespaceId,
            @Valid @RequestBody RemoveRolesFromGroupRequestDto request
    ) {
        log.info("Removing roles {} from group {}", request.roleIds(), groupId);
        groupRoleService.removeRolesFromGroup(namespaceId, groupId, request.roleIds());
        namespaceService.increaseNamespaceVersion(namespaceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/group-id/{groupId}/roles")
    @Operation(summary = "Get group roles",
            description = "Retrieves all roles assigned to a specific group with optional filtering and pagination")
    public ResponseEntity<List<GroupRoleResponseDto>> getGroupRoles(
            @PathVariable String groupId,
            @PathVariable String namespaceId,
            @RequestParam(required = false) String roleName,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<GroupRoleResponseDto> result = groupRoleService.getGroupRoles(namespaceId, groupId, roleName, pageable);
        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(result.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(result.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }

    @GetMapping("/role-id/{roleId}/groups")
    @Operation(summary = "Get role groups",
            description = "Retrieves all groups associated with a specific role with optional filtering and pagination")
    public ResponseEntity<List<GroupResponseDto>> getRoleGroups(
            @PathVariable String roleId,
            @PathVariable String namespaceId,
            @RequestParam(required = false) String groupName,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<GroupResponseDto> result = groupRoleService.getRoleGroups(namespaceId, roleId, groupName, pageable);
        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(result.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(result.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }
}