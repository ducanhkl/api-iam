package org.ducanh.apiiam.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.experimental.FieldNameConstants;
import org.ducanh.apiiam.dto.requests.UserRegisterRequestDto;
import org.ducanh.apiiam.dto.responses.CreateUserResponseDto;
import org.ducanh.apiiam.dto.responses.UserResponseDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "pwd_alg", length = 50)
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private PasswordAlg pwdAlg;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "namespace_id")
    private String namespaceId;

    @Column(name = "status", length = 50)
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private UserStatus status;

    @Column(name = "last_login", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastLogin;

    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled;

    @Column(name = "mfa_secret", length = 255)
    private String mfaSecret;

    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public static User initUser(UserRegisterRequestDto request,
                                String passwordHash,
                                PasswordAlg pwdAlg,
                                String namespaceId) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordHash) // Hashed password
                .pwdAlg(pwdAlg) // Example: assuming bcrypt is the hashing algorithm
                .namespaceId(namespaceId)
                .isVerified(false) // Default: new users are not verified
                .deleted(false) // Default: account is not deleted
                .status(UserStatus.WAITING_FOR_VERIFY) // Default status for a newly registered user
                .mfaEnabled(false) // Default: MFA is not enabled for new users
                .accountLocked(false) // Default: account is not locked
                .createdAt(OffsetDateTime.now()) // Current timestamp
                .updatedAt(OffsetDateTime.now()) // Matches createdAt initially
                .phoneNumber(request.phoneNumber())
                .build();
    }

    public UserResponseDto toUserResponseDto() {
        return UserResponseDto.builder()
                .userId(this.userId)
                .username(this.username)
                .email(this.email)
                .isVerified(this.isVerified)
                .namespaceId(this.namespaceId)
                .status(this.status)
                .lastLogin(this.lastLogin)
                .mfaEnabled(this.mfaEnabled)
                .accountLocked(this.accountLocked)
                .phoneNumber(this.phoneNumber)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public void makeVerified() {
        isVerified = true;
        status = UserStatus.ACTIVE;
    }
}