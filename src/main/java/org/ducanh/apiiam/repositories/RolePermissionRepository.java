package org.ducanh.apiiam.repositories;

import feign.Param;
import org.ducanh.apiiam.entities.Permission;
import org.ducanh.apiiam.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findAllByNamespaceIdAndRoleIdAndPermissionIdIn(String namespaceId, String roleId, Collection<String> permissionId);

    List<RolePermission> findAllByNamespaceIdAndRoleId(String namespaceId, String roleId);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.namespaceId = :namespaceId AND rp.roleId = :roleId AND rp.permissionId IN :permissionIds")
    void deleteAllByNamespaceIdAndRoleIdAndPermissionIdIn(
            @Param("namespaceId") String namespaceId,
            @Param("roleId") String roleId,
            @Param("permissionIds") List<String> permissionIds);
}
