package org.ducanh.apiiam.services;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Role role = Role.from(requestDto);
        Role savedRole = roleRepository.save(role);
        return savedRole.toCreateResponseDto();
    }

    public Page<RoleResponseDto> getRoles(String roleName, Pageable pageable) {
        final Page<Role> rolePage;
        if (Objects.nonNull(roleName) && !roleName.trim().isEmpty()) {
            rolePage = roleRepository.findByRoleNameContainingIgnoreCase(roleName.trim(), pageable);
        } else {
            rolePage = roleRepository.findAll(pageable);
        }
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
