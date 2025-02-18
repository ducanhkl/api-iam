package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, String>, JpaSpecificationExecutor<Group> {
    boolean existsByGroupIdAndNamespaceId(String groupId, String namespaceId);

    default void notExistsByNamespaceIdAndGroupIdOrThrow(String groupId, String namespaceId) {
        Boolean exists = existsByGroupIdAndNamespaceId(groupId, namespaceId);
        if (exists) {
            throw new RuntimeException("Role existed");
        }
    };

    boolean existsAllByNamespaceIdAndGroupIdIn(String namespaceId, List<String> groupIds);
}
