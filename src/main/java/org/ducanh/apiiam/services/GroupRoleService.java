package org.ducanh.apiiam.services;

import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.responses.GroupResponseDto;
import org.ducanh.apiiam.dto.responses.GroupRoleResponseDto;
import org.ducanh.apiiam.entities.Group;
import org.ducanh.apiiam.entities.GroupRole;
import org.ducanh.apiiam.entities.Role;
import org.ducanh.apiiam.repositories.GroupRepository;
import org.ducanh.apiiam.repositories.GroupRoleRepository;
import org.ducanh.apiiam.repositories.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
public class GroupRoleService {
    private final GroupRoleRepository groupRoleRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;

    public GroupRoleService(
            GroupRoleRepository groupRoleRepository,
            GroupRepository groupRepository,
            RoleRepository roleRepository
    ) {
        this.groupRoleRepository = groupRoleRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
    }

    public void assignRolesToGroup(String namespaceId, String groupId, List<String> roleIds) {
        Group group = groupRepository.findGroupByNamespaceIdAndGroupId(namespaceId, groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Verify all roles exist using count
        long existingRolesCount = roleRepository.countAllByNamespaceIdAndRoleIdIn(namespaceId, roleIds);
        if (existingRolesCount != roleIds.size()) {
            throw new RuntimeException("One or more roles not found");
        }

        // Filter out already assigned roles
        List<String> existingRoleIds = groupRoleRepository.findAllByNamespaceIdAndGroupId(groupId)
                .stream()
                .map(GroupRole::getRoleId)
                .toList();

        List<GroupRole> newGroupRoles = roleIds.stream()
                .filter(roleId -> !existingRoleIds.contains(roleId))
                .map(roleId -> GroupRole.builder()
                        .groupId(groupId)
                        .roleId(roleId)
                        .namespaceId(group.getNamespaceId())
                        .build())
                .toList();

        groupRoleRepository.saveAll(newGroupRoles);
    }

    public void removeRolesFromGroup(String namespaceId, String groupId, List<String> roleIds) {
        if (!groupRepository.existsByGroupIdAndNamespaceId(groupId, namespaceId)) {
            throw new RuntimeException("Group not found");
        }
        groupRoleRepository.deleteAllByNamespaceIdAndGroupIdAndRoleIdIn(namespaceId, groupId, roleIds);
    }

    public Page<GroupRoleResponseDto> getGroupRoles(
            String namespaceId,
            String groupId,
            String roleName,
            Pageable pageable
    ) {
        if (!groupRepository.existsById(groupId)) {
            throw new RuntimeException("Group not found");
        }

        Specification<Role> spec = (root, query, cb) -> {
            assert query != null;
            List<Predicate> predicates = new ArrayList<>();
            Subquery<String> groupRoleSubquery = query.subquery(String.class);
            Root<GroupRole> groupRoleRoot = groupRoleSubquery.from(GroupRole.class);
            groupRoleSubquery.select(groupRoleRoot.get(GroupRole.Fields.roleId))
                    .where(cb.and(cb.equal(groupRoleRoot.get(GroupRole.Fields.groupId), groupId),
                            cb.equal(groupRoleRoot.get(GroupRole.Fields.namespaceId), namespaceId)));
            predicates.add(root.get(Role.Fields.roleId).in(groupRoleSubquery));
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
                .map(role -> {
                    GroupRole groupRole = groupRoleRepository
                            .findAllByNamespaceIdAndGroupIdAndRoleIdIn(groupId, List.of(role.getRoleId()))
                            .getFirst();

                    return GroupRoleResponseDto.builder()
                            .roleId(role.getRoleId())
                            .roleName(role.getRoleName())
                            .description(role.getDescription())
                            .assignedAt(groupRole.getAssignedAt())
                            .build();
                });
    }

    public Page<GroupResponseDto> getRoleGroups(
            String namespaceId,
            String roleId,
            String groupName,
            Pageable pageable
    ) {
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role not found");
        }

        Specification<Group> spec = (root, query, cb) -> {
            assert query != null;
            List<Predicate> predicates = new ArrayList<>();
            Subquery<String> groupRoleSubquery = query.subquery(String.class);
            Root<GroupRole> groupRoleRoot = groupRoleSubquery.from(GroupRole.class);
            groupRoleSubquery.select(groupRoleRoot.get(GroupRole.Fields.groupId))
                    .where(cb.equal(groupRoleRoot.get(GroupRole.Fields.roleId), roleId));
            predicates.add(root.get(Group.Fields.groupId).in(groupRoleSubquery));
            predicates.add(cb.equal(root.get(Role.Fields.namespaceId), namespaceId));
            if (StringUtils.hasText(groupName)) {
                predicates.add(cb.like(
                        cb.lower(root.get(Group.Fields.groupName)),
                        "%" + groupName.toLowerCase().trim() + "%"
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return groupRepository.findAll(spec, pageable)
                .map(Group::toGroupResponseDto);
    }
}
