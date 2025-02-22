package org.ducanh.apiiam.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreatePermissionRequestDto;
import org.ducanh.apiiam.dto.requests.IndexPermissionRequestParamsDto;
import org.ducanh.apiiam.dto.requests.UpdatePermissionRequestDto;
import org.ducanh.apiiam.dto.responses.PermissionResponseDto;
import org.ducanh.apiiam.services.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ducanh.apiiam.Constants.*;


@RestController
@RequestMapping("permission/namespace-id/{namespaceId}/")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<PermissionResponseDto> createPermission(
            @Valid @RequestBody CreatePermissionRequestDto request,
            @PathVariable String namespaceId) {
        log.info("Creating permission: {}", request);
        PermissionResponseDto response = permissionService.createPermission(namespaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("permission-id/{permissionId}")
    public ResponseEntity<PermissionResponseDto> getPermission(@PathVariable String permissionId,
                                                               @PathVariable String namespaceId) {
        PermissionResponseDto response = permissionService.getPermission(namespaceId, permissionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/index")
    public ResponseEntity<List<PermissionResponseDto>> indexPermissions(
            @PathVariable String namespaceId,
            IndexPermissionRequestParamsDto params) {
        Pageable pageable = PageRequest.of(params.page(), params.size());
        Page<PermissionResponseDto> permissionPage = permissionService.indexPermissions(namespaceId, params, pageable);

        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(permissionPage.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(permissionPage.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(permissionPage.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(permissionPage.getTotalPages()))
                .body(permissionPage.getContent());
    }

    @PutMapping("permission-id/{permissionId}")
    public ResponseEntity<PermissionResponseDto> updatePermission(
            @PathVariable String permissionId,
            @PathVariable String namespaceId,
            @Valid @RequestBody UpdatePermissionRequestDto request) {
        PermissionResponseDto response = permissionService.updatePermission(namespaceId, permissionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("permission-id/{permissionId}")
    public ResponseEntity<Void> deletePermission(@PathVariable String permissionId,
                                                 @PathVariable String namespaceId) {
        permissionService.deletePermission(namespaceId, permissionId);
        return ResponseEntity.noContent().build();
    }
}
