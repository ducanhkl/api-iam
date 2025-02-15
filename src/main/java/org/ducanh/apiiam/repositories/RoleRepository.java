package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Page<Role> findByRoleNameContainingIgnoreCase(String trim, Pageable pageable);
}
