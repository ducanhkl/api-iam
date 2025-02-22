package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import org.ducanh.apiiam.dto.requests.CreatePermissionRequestDto;
import org.ducanh.apiiam.dto.requests.IndexPermissionRequestParamsDto;
import org.ducanh.apiiam.dto.requests.UpdatePermissionRequestDto;
import org.ducanh.apiiam.dto.responses.PermissionResponseDto;
import org.ducanh.apiiam.entities.Permission;
import org.ducanh.apiiam.repositories.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public PermissionResponseDto createPermission(String namespaceId, CreatePermissionRequestDto request) {
        Permission permission = Permission.builder()
                .permissionId(request.permissionId())
                .permissionName(request.permissionName())
                .description(request.description())
                .namespaceId(namespaceId)
                .build();

        Permission savedPermission = permissionRepository.save(permission);
        return savedPermission.toPermissionResponseDto();
    }

    @Transactional
    public PermissionResponseDto updatePermission(String namespaceId, String permissionId, UpdatePermissionRequestDto request) {
        Permission permission = permissionRepository.findPermissionByNamespaceIdAndPermissionId(namespaceId, permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));

        permission.setPermissionName(request.permissionName());
        permission.setDescription(request.description());
        return permission.toPermissionResponseDto();
    }

    public PermissionResponseDto getPermission(String namespaceId, String permissionId) {
        return permissionRepository.findPermissionByNamespaceIdAndPermissionId(namespaceId, permissionId)
                .map(Permission::toPermissionResponseDto)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));
    }

    public Page<PermissionResponseDto> indexPermissions(String namespaceId, IndexPermissionRequestParamsDto params, Pageable pageable) {
        return permissionRepository.findAll(buildSearchCriteria(namespaceId, params), pageable)
                .map(Permission::toPermissionResponseDto);
    }

    @Transactional
    public void deletePermission(String namespaceId, String permissionId) {
        Permission permission = permissionRepository.findPermissionByNamespaceIdAndPermissionId(namespaceId, permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + permissionId));
        permissionRepository.delete(permission);
    }

    private Specification<Permission> buildSearchCriteria(String namespaceId, IndexPermissionRequestParamsDto params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(
                    root.get(Permission.Fields.namespaceId),
                    namespaceId
            ));
            if (StringUtils.hasText(params.permissionName())) {
                predicates.add(cb.equal(
                        cb.lower(root.get(Permission.Fields.permissionName)),
                        params.permissionName().toLowerCase().trim()
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
