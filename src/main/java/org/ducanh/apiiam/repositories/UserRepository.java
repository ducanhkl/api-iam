package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByUserId(Long id);
    boolean existsByUsernameAndNamespaceId(String username, String namespaceId);
    boolean existsByEmailAndNamespaceId(String email, String namespaceId);
    User findByUsernameAndNamespaceId(String username, String namespaceId);

    default User findByUsernameAndNamespaceIdOrThrow(String username, String namespaceId) {
        User user = findByUsernameAndNamespaceId(username, namespaceId);
        if (Objects.isNull(user)) {
            throw new RuntimeException("Username not existed");
        }
        return user;
    }
}
