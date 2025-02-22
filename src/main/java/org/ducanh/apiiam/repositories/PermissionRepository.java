package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findPermissionByNamespaceIdAndPermissionId(String namespaceId, String permissionId);
}
