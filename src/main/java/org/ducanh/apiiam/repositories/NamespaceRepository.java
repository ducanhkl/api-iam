package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.naming.Name;

@Repository
public interface NamespaceRepository extends JpaRepository<Namespace, String>, JpaSpecificationExecutor<Namespace> {

    Namespace findByNamespaceId(String namespaceId);

    default void existOrThrowById(String namespaceId) {
        if (!existsById(namespaceId)) {
            throw new RuntimeException("Namespace with id " + namespaceId + " does not exist");
        }
    }


}
