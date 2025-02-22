package org.ducanh.apiiam.repositories;


import org.ducanh.apiiam.entities.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRoleRepository extends JpaRepository<GroupRole, Long> {
    List<GroupRole> findAllByNamespaceIdAndGroupIdAndRoleIdIn(String groupId, List<String> roleIds);
    List<GroupRole> findAllByNamespaceIdAndGroupId(String groupId);
    List<GroupRole> findAllByRoleId(String roleId);

    @Modifying
    @Query("DELETE FROM GroupRole gr WHERE gr.namespaceId = :namespaceId AND gr.groupId = :groupId AND gr.roleId IN :roleIds")
    void deleteAllByNamespaceIdAndGroupIdAndRoleIdIn(String namespaceId, String groupId, List<String> roleIds);

    @Modifying
    @Query("DELETE FROM GroupRole gr WHERE gr.namespaceId = :namespaceId AND gr.groupId = :groupId")
    void deleteAllByNamespaceIdAndGroupId(String namespaceId, String groupId);
}
