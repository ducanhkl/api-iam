package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {

    Optional<Role> findROleByNamespaceIdAndRoleId(String namespaceId, String roleId);
    Boolean existsByRoleIdAndNamespaceId(String roleId, String namespaceId);

    default void notExistsByNamespaceIdAndRoleIdOrThrow(String roleId, String namespaceId) {
        Boolean exists = existsByRoleIdAndNamespaceId(roleId, namespaceId);
        if (exists) {
            throw new RuntimeException("Role existed");
        }
    };

    boolean areAllRolesInNamespace(int size, List<String> roleIds, String namespaceId);

    long countAllByNamespaceIdAndRoleIdIn(String namespaceId, List<String> roleIds);
}
