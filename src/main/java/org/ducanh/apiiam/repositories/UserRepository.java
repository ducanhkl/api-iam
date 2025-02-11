package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsernameAndNamespaceId(String username, Long namespaceId);
    boolean existsByEmailAndNamespaceId(String email, Long namespaceId);
    User findByUsernameAndNamespaceId(String username, Long namespaceId);

    default User findByUsernameAndNamespaceIdOrThrow(String username, Long namespaceId) {
        User user = findByUsernameAndNamespaceId(username, namespaceId);
        if (Objects.isNull(user)) {
            throw new RuntimeException("Username not existed");
        }
        return user;
    }
}
