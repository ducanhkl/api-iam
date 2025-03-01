package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.responses.PermissionRoleResponseDto;
import org.ducanh.apiiam.dto.responses.RolePermissionResponseDto;
import org.ducanh.apiiam.entities.Permission;
import org.ducanh.apiiam.entities.Role;
import org.ducanh.apiiam.entities.RolePermission;
import org.ducanh.apiiam.exceptions.CommonException;
import org.ducanh.apiiam.exceptions.ErrorCode;
import org.ducanh.apiiam.repositories.PermissionRepository;
import org.ducanh.apiiam.repositories.RolePermissionRepository;
import org.ducanh.apiiam.repositories.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
public class RolePermissionService {
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionService(
            RolePermissionRepository rolePermissionRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository
    ) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public void assignPermissionsToRole(String namespaceId, String roleId, List<String> permissionIds) {
        roleRepository.findROleByNamespaceIdAndRoleId(namespaceId, roleId)
                .orElseThrow(() -> new CommonException(ErrorCode.ROLE_NOT_FOUND,
                        "roleId: {0}, namespaceId: {1}", roleId, namespaceId));

        long existingPermissionsCount = permissionRepository.countAllByNamespaceIdAndPermissionIdIn(namespaceId, permissionIds);
        if (existingPermissionsCount != permissionIds.size()) {
            throw new CommonException(ErrorCode.PERMISSION_NOT_EXIST,
                    "Some permission not exists");
        }

        List<String> existingPermissionIds = rolePermissionRepository.findAllByNamespaceIdAndRoleId(namespaceId, roleId)
                .stream()
                .map(RolePermission::getPermissionId)
                .toList();

        List<RolePermission> newRolePermissions = permissionIds.stream()
                .filter(permissionId -> !existingPermissionIds.contains(permissionId))
                .map(permissionId -> RolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permissionId)
                        .namespaceId(namespaceId)
                        .build())
                .toList();

        rolePermissionRepository.saveAll(newRolePermissions);
    }

    public void removePermissionsFromRole(String namespaceId, String roleId, List<String> permissionIds) {
        if (!roleRepository.existsByRoleIdAndNamespaceId(roleId, namespaceId)) {
            throw new CommonException(ErrorCode.ROLE_NOT_FOUND,
                    "roleId: {0}, namespaceId: {1}", roleId, namespaceId);
        }
        rolePermissionRepository.deleteAllByNamespaceIdAndRoleIdAndPermissionIdIn(namespaceId, roleId, permissionIds);
    }

    public Page<RolePermissionResponseDto> getRolePermissions(
            String namespaceId,
            String roleId,
            String permissionName,
            Pageable pageable
    ) {
        if (!roleRepository.existsByRoleIdAndNamespaceId(roleId, namespaceId)) {
            throw new CommonException(ErrorCode.ROLE_NOT_FOUND,
                    "roleId: {0}, namespaceId: {1}", roleId, namespaceId);
        }

        Specification<Permission> spec = (root, query, cb) -> {
            assert query != null;
            List<Predicate> predicates = new ArrayList<>();

            Subquery<String> rolePermissionSubquery = query.subquery(String.class);
            Root<RolePermission> rolePermissionRoot = rolePermissionSubquery.from(RolePermission.class);
            rolePermissionSubquery.select(rolePermissionRoot.get(RolePermission.Fields.permissionId))
                    .where(cb.and(
                            cb.equal(rolePermissionRoot.get(RolePermission.Fields.roleId), roleId),
                            cb.equal(rolePermissionRoot.get(RolePermission.Fields.namespaceId), namespaceId)
                    ));

            predicates.add(root.get(Permission.Fields.permissionId).in(rolePermissionSubquery));
            predicates.add(cb.equal(root.get(Permission.Fields.namespaceId), namespaceId));

            if (StringUtils.hasText(permissionName)) {
                predicates.add(cb.like(
                        cb.lower(root.get(Permission.Fields.permissionName)),
                        "%" + permissionName.toLowerCase().trim() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return permissionRepository.findAll(spec, pageable)
                .map(permission -> RolePermissionResponseDto.builder()
                        .permissionId(permission.getPermissionId())
                        .permissionName(permission.getPermissionName())
                        .description(permission.getDescription())
                        .build());
    }

    public Page<PermissionRoleResponseDto> getPermissionRoles(
            String namespaceId,
            String permissionId,
            String roleName,
            Pageable pageable
    ) {
        if (!permissionRepository.existsByPermissionIdAndNamespaceId(permissionId, namespaceId)) {
            throw new CommonException(ErrorCode.PERMISSION_NOT_EXIST,
                    "permissionId: {0} namespaceId: {1}", permissionId, namespaceId);
        }

        Specification<Role> spec = (root, query, cb) -> {
            assert query != null;
            List<Predicate> predicates = new ArrayList<>();
            Subquery<String> rolePermissionSubquery = query.subquery(String.class);
            Root<RolePermission> rolePermissionRoot = rolePermissionSubquery.from(RolePermission.class);
            rolePermissionSubquery.select(rolePermissionRoot.get(RolePermission.Fields.roleId))
                    .where(cb.and(
                            cb.equal(rolePermissionRoot.get(RolePermission.Fields.permissionId), permissionId),
                            cb.equal(rolePermissionRoot.get(RolePermission.Fields.namespaceId), namespaceId)
                    ));
            predicates.add(root.get(Role.Fields.roleId).in(rolePermissionSubquery));
            predicates.add(cb.equal(root.get(Role.Fields.namespaceId), namespaceId));
            if (StringUtils.hasText(roleName)) {
                predicates.add(cb.like(
                        cb.lower(root.get(Role.Fields.roleName)),
                        "%" + roleName.toLowerCase().trim() + "%"
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return roleRepository.findAll(spec, pageable)
                .map((role) -> PermissionRoleResponseDto.builder()
                        .roleId(role.getRoleId())
                        .roleName(role.getRoleName())
                        .description(role.getDescription()).build());
    }
}