package org.ducanh.apiiam.repositories;


import org.ducanh.apiiam.entities.GroupRole;
import org.ducanh.apiiam.entities.GroupRoleIdOnly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRoleRepository extends JpaRepository<GroupRole, Long> {
    List<GroupRole> findAllByNamespaceIdAndGroupId(String namespaceId, String groupId);

    @Modifying
    @Query("DELETE FROM GroupRole gr WHERE gr.namespaceId = :namespaceId AND gr.groupId = :groupId AND gr.roleId IN :roleIds")
    void deleteAllByNamespaceIdAndGroupIdAndRoleIdIn(String namespaceId, String groupId, List<String> roleIds);

    @Modifying
    void deleteAllByRoleIdAndNamespaceId(String roleId, String namespaceId);

    @Modifying
    void deleteAllByGroupIdAndNamespaceId(String groupId, String namespaceId);

    @Query("""
        SELECT gr.roleId FROM GroupRole gr WHERE gr.groupId = :groupId AND gr.roleId IN (:roleIds) AND gr.namespaceId = :namespaceId
        """)
    List<String> findExistedRoleId(String groupId, String namespaceId, List<String> roleIds);

    @Query("""
        SELECT new org.ducanh.apiiam.entities.GroupRoleIdOnly(gr.groupId, gr.roleId, gr.namespaceId)
            FROM GroupRole gr
                WHERE gr.namespaceId = :namespaceId
    """)
    List<GroupRoleIdOnly> findAllByNamespaceId(String namespaceId);

}
