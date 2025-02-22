package org.ducanh.apiiam.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateRoleRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateRoleRequestDto;
import org.ducanh.apiiam.dto.responses.CreateRoleResponseDto;
import org.ducanh.apiiam.dto.responses.RoleResponseDto;
import org.ducanh.apiiam.dto.responses.UpdateRoleResponseDto;
import org.ducanh.apiiam.services.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ducanh.apiiam.Constants.*;

@RestController
@RequestMapping("role/namespace-id/{namespaceId}")
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<CreateRoleResponseDto> createRole(
            @Valid @RequestBody CreateRoleRequestDto requestDto,
            @PathVariable String namespaceId) {
        log.info("Creating new role: {}", requestDto);
        CreateRoleResponseDto response = roleService.createRole(namespaceId, requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("index")
    public ResponseEntity<List<RoleResponseDto>> getRoles(
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable(name = "namespaceId") String namespaceId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoleResponseDto> rolePage = roleService.getRoles(namespaceId, roleName, pageable);

        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(rolePage.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(rolePage.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(rolePage.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(rolePage.getTotalPages()))
                .body(rolePage.getContent());
    }

    @GetMapping("role-id/{roleId}")
    public ResponseEntity<RoleResponseDto> getRole(@PathVariable String roleId, @PathVariable String namespaceId) {
        log.info("Fetching role with id: {}", roleId);
        RoleResponseDto response = roleService.getRole(namespaceId, roleId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("role-id/{roleId}")
    public ResponseEntity<UpdateRoleResponseDto> updateRole(
            @PathVariable String roleId,
            @PathVariable String namespaceId,
            @Valid @RequestBody UpdateRoleRequestDto requestDto) {
        log.info("Updating role with id: {}", roleId);
        UpdateRoleResponseDto response = roleService.updateRole(namespaceId, roleId, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("role-id/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleId,
                                           @PathVariable String namespaceId) {
        log.info("Deleting role with id: {}", roleId);
        roleService.deleteRole(namespaceId, roleId);
        return ResponseEntity.noContent().build();
    }
}
