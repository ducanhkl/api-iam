package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateRoleRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateRoleRequestDto;
import org.ducanh.apiiam.dto.responses.CreateRoleResponseDto;
import org.ducanh.apiiam.dto.responses.RoleResponseDto;
import org.ducanh.apiiam.dto.responses.UpdateRoleResponseDto;
import org.ducanh.apiiam.entities.Role;
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

    public CreateRoleResponseDto createRole(CreateRoleRequestDto requestDto) {
        roleRepository.notExistsByNamespaceIdAndRoleIdOrThrow(requestDto.roleId(), requestDto.namespaceId());
        Role role = Role.from(requestDto);
        Role savedRole = roleRepository.save(role);
        return savedRole.toCreateResponseDto();
    }

    public Page<RoleResponseDto> getRoles(String namespaceId, String roleName, Pageable pageable) {
        final Page<Role> rolePage;
        Specification<Role> specification = ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get(Role.Fields.namespaceId), namespaceId));
            if (Objects.nonNull(roleName) && !roleName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get(Role.Fields.roleName),
                        "%" + roleName.toLowerCase().trim() + "&"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
        rolePage = roleRepository.findAll(specification, pageable);
        return rolePage.map(Role::toResponseDto);
    }

    public RoleResponseDto getRole(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        return role.toResponseDto();
    }

    @Transactional
    public UpdateRoleResponseDto updateRole(String id, UpdateRoleRequestDto requestDto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        role.update(requestDto);
        return role.toUpdateResponseDto();
    }

    public void deleteRole(String id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

}
