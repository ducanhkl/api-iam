package org.ducanh.apiiam.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.AssignPermissionsToRoleRequestDto;
import org.ducanh.apiiam.dto.requests.RemovePermissionsFromRoleRequestDto;
import org.ducanh.apiiam.dto.responses.PermissionRoleResponseDto;
import org.ducanh.apiiam.dto.responses.RolePermissionResponseDto;
import org.ducanh.apiiam.services.RolePermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ducanh.apiiam.Constants.*;

@RestController
@RequestMapping("/role-permission/{namespaceId}")
@Slf4j
@RequiredArgsConstructor
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

    @PostMapping("/role-id/{roleId}/permissions")
    public ResponseEntity<Void> assignPermissionsToRole(
            @PathVariable String roleId,
            @PathVariable String namespaceId,
            @Valid @RequestBody AssignPermissionsToRoleRequestDto request
    ) {
        log.info("Assigning permissions {} to role {}", request.permissionIds(), roleId);
        rolePermissionService.assignPermissionsToRole(namespaceId, roleId, request.permissionIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/role-id/{roleId}/permissions")
    public ResponseEntity<Void> removePermissionsFromRole(
            @PathVariable String roleId,
            @PathVariable String namespaceId,
            @Valid @RequestBody RemovePermissionsFromRoleRequestDto request
    ) {
        log.info("Removing permissions {} from role {}", request.permissionIds(), roleId);
        rolePermissionService.removePermissionsFromRole(namespaceId, roleId, request.permissionIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/role-id/{roleId}/permissions")
    public ResponseEntity<List<RolePermissionResponseDto>> getRolePermissions(
            @PathVariable String roleId,
            @PathVariable String namespaceId,
            @RequestParam(required = false) String permissionName,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<RolePermissionResponseDto> result = rolePermissionService.getRolePermissions(namespaceId, roleId, permissionName, pageable);
        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(result.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(result.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }

    @GetMapping("/permission-id/{permissionId}/roles")
    public ResponseEntity<List<PermissionRoleResponseDto>> getPermissionRoles(
            @PathVariable String permissionId,
            @PathVariable String namespaceId,
            @RequestParam(required = false) String roleName,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PermissionRoleResponseDto> result = rolePermissionService.getPermissionRoles(namespaceId, permissionId, roleName, pageable);
        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(result.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(result.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(result.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(result.getTotalPages()))
                .body(result.getContent());
    }
}