package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("""
        select count(*) from Session session where session.userId = :userId
        """)
    Integer countSessionByUserId(@Param("userId") Long userId);

    Session findSessionByRefreshTokenId(String refreshTokenId);
}
