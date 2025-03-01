package org.ducanh.apiiam.repositories;

import jakarta.validation.GroupDefinitionException;
import org.ducanh.apiiam.entities.Group;
import org.ducanh.apiiam.exceptions.CommonException;
import org.ducanh.apiiam.exceptions.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.w3c.dom.DOMException;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String>, JpaSpecificationExecutor<Group> {

    Optional<Group> findGroupByNamespaceIdAndGroupId(String namespaceId, String groupId);
    boolean existsAllByNamespaceIdAndGroupIdIn(String namespaceId, List<String> groupIds);
    boolean existsByGroupIdAndNamespaceId(String groupId, String namespaceId);
    default void notExistsByNamespaceIdAndGroupIdOrThrow(String groupId, String namespaceId) {
        boolean exists = existsByGroupIdAndNamespaceId(groupId, namespaceId);
        if (exists) {
            throw new CommonException(ErrorCode.GROUP_INFO_DUPLICATED,
                    "GroupId {0}, namespace: {1} are duplicated", groupId, namespaceId);
        }
    };
}
