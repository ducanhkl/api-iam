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
@RequestMapping("role")
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<CreateRoleResponseDto> createRole(@Valid @RequestBody CreateRoleRequestDto requestDto) {
        log.info("Creating new role: {}", requestDto);
        CreateRoleResponseDto response = roleService.createRole(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> getRoles(
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoleResponseDto> rolePage = roleService.getRoles(roleName, pageable);

        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(rolePage.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(rolePage.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(rolePage.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(rolePage.getTotalPages()))
                .body(rolePage.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getRole(@PathVariable String id) {
        log.info("Fetching role with id: {}", id);
        RoleResponseDto response = roleService.getRole(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateRoleResponseDto> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequestDto requestDto) {
        log.info("Updating role with id: {}", id);
        UpdateRoleResponseDto response = roleService.updateRole(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        log.info("Deleting role with id: {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
