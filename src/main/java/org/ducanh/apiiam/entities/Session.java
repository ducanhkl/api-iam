package org.ducanh.apiiam.entities;


import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sessions_id_seq")
    @SequenceGenerator(
            name = "sessions_id_seq",
            sequenceName = "sessions_id_seq",
            allocationSize = 100
    )
    @Column(name = "sessionId")
    private Long sessionId;

    @Column(name = "access_token_id")
    private String accessTokenId;

    @Column(name = "refresh_token_id")
    private String refreshTokenId;

    @Column(name = "refreshTokenIssueAt", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime refreshTokenIssueAt;

    @Column(name = "refreshTokenExpiredAt", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime refreshTokenExpiredAt;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "active")
    private boolean active;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "namespaceId")
    private String namespaceId;

    @Column(name = "userAgent", length = 512)
    private String userAgent;

    @Column(name = "ipAddress", length = 45) // Supports IPv6 (45 chars max)
    private String ipAddress;

    @Column(name = "kid", length = 100)
    private Long kid; // Key ID for JWT signing key used

    @Column(name = "sessionType", length = 20)
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}