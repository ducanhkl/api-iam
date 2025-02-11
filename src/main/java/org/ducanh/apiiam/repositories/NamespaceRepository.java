package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NamespaceRepository extends JpaRepository<Namespace, Long> {

    Namespace findByNamespaceId(Long namespaceId);

    default void existOrThrowById(Long namespaceId) {
        if (!existsById(namespaceId)) {
            throw new RuntimeException("Namespace with id " + namespaceId + " does not exist");
        }
    }

}
