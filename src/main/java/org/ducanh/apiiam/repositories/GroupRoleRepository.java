package org.ducanh.apiiam.repositories;


import org.ducanh.apiiam.entities.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRoleRepository extends JpaRepository<GroupRole, Long> {
    List<GroupRole> findAllByGroupIdAndRoleIdIn(String groupId, List<String> roleIds);
    List<GroupRole> findAllByGroupId(String groupId);
    List<GroupRole> findAllByRoleId(String roleId);

    @Modifying
    @Query("DELETE FROM GroupRole gr WHERE gr.groupId = ?1 AND gr.roleId IN ?2")
    void deleteAllByGroupIdAndRoleIdIn(String groupId, List<String> roleIds);

    @Modifying
    @Query("DELETE FROM GroupRole gr WHERE gr.groupId = ?1")
    void deleteAllByGroupId(String groupId);

    @Modifying
    @Query("DELETE FROM GroupRole gr WHERE gr.roleId = ?1")
    void deleteAllByRoleId(String roleId);

    boolean existsByGroupIdAndRoleId(String groupId, String roleId);
}
