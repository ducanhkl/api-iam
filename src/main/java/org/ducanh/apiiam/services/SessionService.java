package org.ducanh.apiiam.services;

import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.entities.KeyPair;
import org.ducanh.apiiam.entities.Session;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static org.ducanh.apiiam.helpers.ValidationHelpers.valArg;

@Service
@Slf4j
public class SessionService {
    
    private final SessionRepository sessionRepository;
    private final Integer maxUserActiveSession;

    @Autowired
    public SessionService(final SessionRepository sessionRepository,
                          @Value("${app.auth.max-user-active-session}")
                          final Integer maxUserActiveSession) {
        this.sessionRepository = sessionRepository;
        this.maxUserActiveSession = maxUserActiveSession;
    }

    public void checkMaxUserActiveSession(User user) {
        Integer countSessionByUserId = sessionRepository.countSessionByUserId(user.getUserId(), true);
        if (countSessionByUserId > maxUserActiveSession) {
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
                .kid(keyPair.getKeyPairId())
                .sessionType(Session.SessionType.WEB)
                .active(true)
                .build();
        return sessionRepository.save(session);
    }

    public void revokeOldSession(Session session) {
        valArg(!session.isRevoked(), () -> new RuntimeException("Token is invalid"));
        valArg(session.isActive(), () -> new RuntimeException("Token inactive"));
        session.setRevoked(true);
        session.setActive(false);
        sessionRepository.save(session);
    }

    public void deactivateSession(String refreshTokenId) {
        Session session = sessionRepository.findSessionByRefreshTokenId(refreshTokenId);
        session.setActive(false);
        sessionRepository.save(session);
    }

    public void deactiveAllActiveSessions(User user) {

    }
}
