package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("""
        select count(*) from Session session where session.userId = :userId AND session.active = :active
        """)
    Integer countSessionByUserId(@Param("userId") Long userId,@Param("active") boolean active);

    Session findSessionByRefreshTokenId(String refreshTokenId);

    @Modifying
    @Query("""
        update Session s set s.active = false where  s.userId = :userId
        """)
    int logoutSession(Long userId);
}
