package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long>, JpaSpecificationExecutor<UserGroup> {

    List<UserGroup> findAllUserGroupsByUserIdAndGroupIdIn(Long userId, List<String> groupIds);


    @Query("""
        SELECT ug.groupId FROM UserGroup ug
        WHERE ug.userId = :userId
            AND ug.groupId NOT IN :groupIds
        """)
    List<String> findGroupIdsByUserIdAndGroupIdNotIn(@Param("userId") Long userId,
                                                     @Param("groupIds") List<String> groupIds);

    @Modifying
    @Query("""
        DELETE FROM UserGroup ug
        WHERE ug.userId = :userId AND ug.groupId in :groupIds
        """)
    int removeUserFromGroups(Long userId, List<String> groupIds);
}
