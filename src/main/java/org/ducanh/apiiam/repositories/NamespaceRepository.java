package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NamespaceRepository extends JpaRepository<Namespace, String>, JpaSpecificationExecutor<Namespace> {

    Namespace findByNamespaceId(String namespaceId);

    default void existOrThrowById(String namespaceId) {
        if (!existsById(namespaceId)) {
            throw new RuntimeException("Namespace with id " + namespaceId + " does not exist");
        }
    }

    @Modifying
    @Query("""
        UPDATE Namespace n
                SET n.version = n.version+ :increment
                        WHERE n.namespaceId = :namespaceId
        """)
    int increaseNamespaceVersion(String namespaceId, Long increment);

}
