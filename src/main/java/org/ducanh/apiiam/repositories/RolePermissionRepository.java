package org.ducanh.apiiam.repositories;

import feign.Param;
import org.ducanh.apiiam.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    @Modifying
    void deleteAllByRoleIdAndNamespaceId(String roleId, String namespaceId);

    @Modifying
    void deleteAllByPermissionIdAndNamespaceId(String permissionId, String namespaceId);

    @Query("""
        SELECT rp.permissionId FROM RolePermission rp
                WHERE rp.namespaceId=:namespaceId AND rp.roleId=:roleId AND rp.permissionId IN (:permissionIds)
        """)
    List<String> findExistedPermissionIds(String roleId, String namespaceId, List<String> permissionIds);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.namespaceId = :namespaceId AND rp.roleId = :roleId AND rp.permissionId IN :permissionIds")
    void deleteAllByNamespaceIdAndRoleIdAndPermissionIdIn(
            @Param("namespaceId") String namespaceId,
            @Param("roleId") String roleId,
            @Param("permissionIds") List<String> permissionIds);

    List<RolePermission> findAllByNamespaceIdAndRoleId(String namespaceId, String roleId);
}
