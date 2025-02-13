package org.ducanh.apiiam.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    public enum SessionType {
        WEB,
        MOBILE,
        CLI,
        API,
        SERVICE_ACCOUNT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sessionId", nullable = false, updatable = false)
    private Long sessionId;

    @Column(name = "access_token_id", nullable = false, unique = true)
    private String accessTokenId;

    @Column(name = "refresh_token_id", nullable = false, unique = true)
    private String refreshTokenId;

    @Column(name = "refreshTokenIssueAt", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime refreshTokenIssueAt;

    @Column(name = "refreshTokenExpiredAt", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime refreshTokenExpiredAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "namespaceId", nullable = false)
    private Long namespaceId;

    @Column(name = "userAgent", length = 512)
    private String userAgent;

    @Column(name = "ipAddress", length = 45) // Supports IPv6 (45 chars max)
    private String ipAddress;

    @Column(name = "kid", length = 100)
    private String kid; // Key ID for JWT signing key used

    @Column(name = "sessionType", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}