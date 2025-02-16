package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {
    Boolean existsByRoleIdAndNamespaceId(String roleId, String namespaceId);

    default void notExistsByNamespaceIdAndRoleIdOrThrow(String roleId, String namespaceId) {
        Boolean exists = existsByRoleIdAndNamespaceId(roleId, namespaceId);
        if (exists) {
            throw new RuntimeException("Role existed");
        }
    };
}
