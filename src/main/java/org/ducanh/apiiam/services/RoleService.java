package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateRoleRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateRoleRequestDto;
import org.ducanh.apiiam.dto.responses.CreateRoleResponseDto;
import org.ducanh.apiiam.dto.responses.RoleResponseDto;
import org.ducanh.apiiam.dto.responses.UpdateRoleResponseDto;
import org.ducanh.apiiam.entities.Role;
import org.ducanh.apiiam.exceptions.CommonException;
import org.ducanh.apiiam.exceptions.ErrorCode;
import org.ducanh.apiiam.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class RoleService {

    public final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public CreateRoleResponseDto createRole(String namespaceId, CreateRoleRequestDto requestDto) {
        roleRepository.notExistsByNamespaceIdAndRoleIdOrThrow(requestDto.roleId(), namespaceId);
        Role role = Role.from(namespaceId, requestDto);
        Role savedRole = roleRepository.save(role);
        return savedRole.toCreateResponseDto();
    }

    public Page<RoleResponseDto> getRoles(String namespaceId, String roleName, Pageable pageable) {
        final Page<Role> rolePage;
        Specification<Role> specification = ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get(Role.Fields.namespaceId), namespaceId));
            if (Objects.nonNull(roleName) && !roleName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(Role.Fields.roleName)),
                        "%" + roleName.toLowerCase().trim() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
        rolePage = roleRepository.findAll(specification, pageable);
        return rolePage.map(Role::toResponseDto);
    }

    public RoleResponseDto getRole(String namespaceId, String roleId) {
        Role role = roleRepository.findROleByNamespaceIdAndRoleId(namespaceId, roleId)
                .orElseThrow(() -> new CommonException(ErrorCode.ROLE_NOT_FOUND,
                        "Role not found with id: {0}, namespaceId: {1}", roleId, namespaceId));
        return role.toResponseDto();
    }

    @Transactional
    public UpdateRoleResponseDto updateRole(String namespaceId, String roleId, UpdateRoleRequestDto requestDto) {
        Role role = roleRepository.findROleByNamespaceIdAndRoleId(namespaceId, roleId)
                .orElseThrow(() -> new CommonException(ErrorCode.ROLE_NOT_FOUND,
                        "Role not found with id: {0}, namespaceId: {1}", roleId, namespaceId));
        role.update(requestDto);
        return role.toUpdateResponseDto();
    }

    @Transactional
    public void deleteRole(String namespaceId, String roleId) {
        Role role = roleRepository.findROleByNamespaceIdAndRoleId(namespaceId, roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        roleRepository.delete(role);
    }

}
