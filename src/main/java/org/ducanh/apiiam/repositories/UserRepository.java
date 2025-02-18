package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.User;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static org.ducanh.apiiam.helpers.ValidationHelpers.valArg;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByUserId(Long id);
    boolean existsByUsernameAndNamespaceId(String username, String namespaceId);
    boolean existsByEmailAndNamespaceId(String email, String namespaceId);
    User findByUsernameAndNamespaceId(String username, String namespaceId);

    default User findByUserIdOrThrow(Long userId) {
        User user = findByUserId(userId);
        valArg(Objects.nonNull(user), () -> new RuntimeException("User not exist"));
        return user;
    }
}
