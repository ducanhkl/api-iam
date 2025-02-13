package org.ducanh.apiiam.services;

import org.ducanh.apiiam.entities.KeyPair;
import org.ducanh.apiiam.entities.Session;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.repositories.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.ducanh.apiiam.helpers.ValidationHelpers.valArg;

@Service
public class SessionService {
    
    private final SessionRepository sessionRepository;

    public SessionService(final SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void checkMaxUserSession(User user) {
        Integer countSessionByUserId = sessionRepository.countSessionByUserId(user.getUserId());
        if (countSessionByUserId > 3) {
            throw new RuntimeException("Exceeded maximum number of sessions");
        }
    }

    public Session createSession(User user,
                              KeyPair keyPair,
                              String userAgent,
                              String ipAddress,
                              String accessTokenId,
                              String refreshTokenId,
                              OffsetDateTime refreshTokenIssueAt,
                              OffsetDateTime refreshTokenExpiredAt) {
        var session = Session.builder()
                .accessTokenId(accessTokenId)
                .refreshTokenId(refreshTokenId)
                .refreshTokenIssueAt(refreshTokenIssueAt)
                .refreshTokenExpiredAt(refreshTokenExpiredAt)
                .revoked(false)
                .userId(user.getUserId())
                .namespaceId(user.getNamespaceId())
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .kid(keyPair.getKid())
                .sessionType(Session.SessionType.WEB)
                .active(true)
                .build();
        return sessionRepository.save(session);
    }

    public void revokeOldSession(Session session) {
        valArg(!session.isRevoked(), () -> new RuntimeException("Token is invalid"));
        valArg(session.isActive(), () -> new RuntimeException("Token inactive"));
        session.setRevoked(true);
        sessionRepository.save(session);
    }

    public void deactivateSession(String refreshTokenId) {
        Session session = sessionRepository.findSessionByRefreshTokenId(refreshTokenId);
        session.setActive(false);
        sessionRepository.save(session);
    }
}
